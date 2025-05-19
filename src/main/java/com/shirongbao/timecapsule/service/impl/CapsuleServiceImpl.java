package com.shirongbao.timecapsule.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timecapsule.common.enums.IsDeletedEnum;
import com.shirongbao.timecapsule.common.enums.PublicStatusEnum;
import com.shirongbao.timecapsule.common.enums.StatusEnum;
import com.shirongbao.timecapsule.common.enums.UnlockedStatusEnum;
import com.shirongbao.timecapsule.converter.CapsuleConverter;
import com.shirongbao.timecapsule.dao.CapsuleMapper;
import com.shirongbao.timecapsule.pojo.bo.CapsuleBo;
import com.shirongbao.timecapsule.pojo.entity.Capsule;
import com.shirongbao.timecapsule.service.CapsuleService;
import com.shirongbao.timecapsule.strategy.CapsuleStrategy;
import com.shirongbao.timecapsule.strategy.CapsuleStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description:
 */
@Service("capsuleService")
@RequiredArgsConstructor
public class CapsuleServiceImpl extends ServiceImpl<CapsuleMapper, Capsule> implements CapsuleService {

    private final CapsuleStrategyFactory capsuleStrategyFactory;

    @Override
    public List<CapsuleBo> queryMyUnlockingCapsuleList() {
        long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<Capsule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Capsule::getUserId, userId);
        wrapper.eq(Capsule::getUnlockedStatus, UnlockedStatusEnum.LOCK.getCode());
        wrapper.eq(Capsule::getCapsuleStatus, StatusEnum.NORMAL.getCode());
        wrapper.eq(Capsule::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        // 根据解锁日期排序
        wrapper.orderByAsc(Capsule::getUnlockTime);
        // 最多要4条即可！
        wrapper.last("limit 4");

        List<Capsule> capsuleList = list(wrapper);
        if (CollectionUtils.isEmpty(capsuleList)) {
            return Collections.emptyList();
        }

        // 转成boList
        List<CapsuleBo> capsuleBoList = CapsuleConverter.INSTANCE.capsuleListToCapsuleBoList(capsuleList);
        for (CapsuleBo capsuleBo : capsuleBoList) {
            // 计算还有几天解锁
            int unlockDays = (int) ((capsuleBo.getUnlockTime().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24));
            // 别出现0天这种情况！
            unlockDays += 1;
            capsuleBo.setUnlockDays(unlockDays);
        }
        return capsuleBoList;
    }

    @Override
    public void unlockCapsule(Long capsuleId) {
        // 先拿到这个capsule
        Capsule capsule = getById(capsuleId);
        if (capsule == null || capsule.getUserId() != StpUtil.getLoginIdAsLong()) {
            throw new RuntimeException("当前数据异常，请稍后再试！");
        }

        // 设置解锁信息
        capsule.setUnlockedStatus(UnlockedStatusEnum.UNLOCK.getCode());
        Integer publicStatus = capsule.getPublicStatus();
        if (publicStatus == PublicStatusEnum.PUBLIC.getCode()) {
            capsule.setPublicTime(new Date());
        }

        updateById(capsule);

        // todo 后面有了公开表，要修改公开表的内容

        // 根据不同的type，执行不同的策略
        Integer capsuleType = capsule.getCapsuleType();
        CapsuleStrategy strategy = capsuleStrategyFactory.getStrategy(capsuleType);
        strategy.unlockCapsule(capsule);
    }

}
