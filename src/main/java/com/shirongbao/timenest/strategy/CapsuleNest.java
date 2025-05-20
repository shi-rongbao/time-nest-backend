package com.shirongbao.timenest.strategy;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.shirongbao.timenest.common.enums.NestTypeEnum;
import com.shirongbao.timenest.common.enums.UnlockedStatusEnum;
import com.shirongbao.timenest.converter.TimeNestConverter;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
        // 设置共同发布好友id
        List<Long> friendIdList = timeNestBo.getFriendIdList();
        if (CollectionUtils.isEmpty(friendIdList)) {
            friendIdList = new ArrayList<>();
        }
        friendIdList = friendIdList.stream().sorted().collect(Collectors.toList());
        String friendIds = JSON.toJSONString(friendIdList);
        timeNest.setFriendIds(friendIds);

        // 设置解锁通知用户id
        List<Long> unlockToUserIdList = timeNestBo.getUnlockToUserIdList();
        if (CollectionUtils.isEmpty(unlockToUserIdList)) {
            unlockToUserIdList = new ArrayList<>();

        }
        // 邀请好友默认也会通知
        List<Long> mergedList = Stream.concat(friendIdList.stream(), unlockToUserIdList.stream())
                .distinct()
                .sorted()
                .toList();

        // 如果没有选择解锁人，默认自己和好友都解锁
        unlockToUserIdList.add(StpUtil.getLoginIdAsLong());
        unlockToUserIdList.addAll(mergedList);
        // 去重
        unlockToUserIdList = unlockToUserIdList.stream().distinct().sorted().collect(Collectors.toList());
        String unlockToUserIds = JSON.toJSONString(unlockToUserIdList);
        timeNest.setUnlockToUserIds(unlockToUserIds);
        timeNest.setUnlockedStatus(UnlockedStatusEnum.LOCK.getCode());
        return timeNest;
    }

}
