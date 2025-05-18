package com.shirongbao.timecapsule.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timecapsule.common.enums.IsDeletedEnum;
import com.shirongbao.timecapsule.common.enums.StatusEnum;
import com.shirongbao.timecapsule.common.enums.UnlockedStatusEnum;
import com.shirongbao.timecapsule.converter.CapsuleConverter;
import com.shirongbao.timecapsule.dao.CapsuleMapper;
import com.shirongbao.timecapsule.pojo.bo.CapsuleBo;
import com.shirongbao.timecapsule.pojo.entity.Capsule;
import com.shirongbao.timecapsule.service.CapsuleService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description:
 */
@Service("capsuleService")
public class CapsuleServiceImpl extends ServiceImpl<CapsuleMapper, Capsule> implements CapsuleService {

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
            unlockDays = unlockDays == 0 ? 1 : unlockDays;
            capsuleBo.setUnlockDays(unlockDays);
        }
        return capsuleBoList;
    }

}
