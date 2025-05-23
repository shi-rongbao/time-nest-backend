package com.shirongbao.timenest.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.common.enums.PublicStatusEnum;
import com.shirongbao.timenest.common.enums.UnlockedStatusEnum;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import com.shirongbao.timenest.service.TimeNestService;
import com.shirongbao.timenest.strategy.NestStrategy;
import com.shirongbao.timenest.strategy.NestStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 拾光纪解锁定时任务类
 */
@Component
@RequiredArgsConstructor
public class TimeNestUnlockSchedule {

    private final TimeNestService timeNestService;

    // 每分钟扫描一次
    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional
    public void timeNestUnlockSchedule() {
        // 查timeNest表中全部未解锁且时间在现在之后的
        LambdaQueryWrapper<TimeNest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeNest::getUnlockedStatus, UnlockedStatusEnum.LOCK.getCode());
        wrapper.eq(TimeNest::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        wrapper.le(TimeNest::getUnlockTime, new Date());

        List<TimeNest> timeNestList = timeNestService.list(wrapper);
        if (CollectionUtils.isEmpty(timeNestList)) {
            return;
        }

        // 批量解锁拾光纪
        for (TimeNest timeNest : timeNestList) {
            timeNestService.unlockNest(timeNest.getId());
        }
    }

}
