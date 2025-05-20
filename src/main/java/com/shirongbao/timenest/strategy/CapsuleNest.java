package com.shirongbao.timenest.strategy;

import com.alibaba.fastjson2.JSON;
import com.shirongbao.timenest.common.enums.NestTypeEnum;
import com.shirongbao.timenest.common.enums.UnlockedStatusEnum;
import com.shirongbao.timenest.converter.TimeNestConverter;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 胶囊nest
 */
@Component
public class CapsuleNest implements NestStrategy {

    @Override
    public int getCode() {
        return NestTypeEnum.CAPSULE.getCode();
    }

    @Override
    public void unlockTimeNest(TimeNest timeNest) {
        // todo 这里通知解锁后提醒谁看
    }

    @Override
    public TimeNest createTimeNest(TimeNestBo timeNestBo) {
        TimeNest timeNest = TimeNestConverter.INSTANCE.timeNestBoToTimeNest(timeNestBo);
        List<Long> friendIdList = timeNestBo.getFriendIdList();
        String friendIds = JSON.toJSONString(friendIdList);
        timeNest.setFriendIds(friendIds);
        List<Long> unlockToUserIdList = timeNestBo.getUnlockToUserIdList();
        // 邀请好友默认也会通知
        unlockToUserIdList = Stream.concat(friendIdList.stream(), unlockToUserIdList.stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        String unlockToUserIds = JSON.toJSONString(unlockToUserIdList);
        timeNest.setUnlockToUserIds(unlockToUserIds);
        timeNest.setUnlockedStatus(UnlockedStatusEnum.LOCK.getCode());
        return timeNest;
    }

}
