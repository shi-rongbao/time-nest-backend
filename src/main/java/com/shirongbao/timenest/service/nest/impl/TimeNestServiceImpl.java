package com.shirongbao.timenest.service.nest.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.constant.BusinessConstant;
import com.shirongbao.timenest.common.enums.IsDeletedEnum;
import com.shirongbao.timenest.common.enums.PublicStatusEnum;
import com.shirongbao.timenest.common.enums.StatusEnum;
import com.shirongbao.timenest.common.enums.UnlockedStatusEnum;
import com.shirongbao.timenest.common.exception.BusinessException;
import com.shirongbao.timenest.converter.TimeNestConverter;
import com.shirongbao.timenest.dao.TimeNestMapper;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.dto.TimeNestDto;
import com.shirongbao.timenest.pojo.entity.PublicTimeNest;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import com.shirongbao.timenest.pojo.entity.UserLikes;
import com.shirongbao.timenest.service.auth.UserService;
import com.shirongbao.timenest.service.nest.PublicTimeNestService;
import com.shirongbao.timenest.service.nest.TimeNestService;
import com.shirongbao.timenest.service.nest.UserLikesService;
import com.shirongbao.timenest.service.notification.NotificationService;
import com.shirongbao.timenest.service.oss.OssService;
import com.shirongbao.timenest.strategy.nest.NestStrategy;
import com.shirongbao.timenest.strategy.nest.NestStrategyFactory;
import com.shirongbao.timenest.utils.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 拾光纪服务实现类
 */
@Service("timeNestService")
@RequiredArgsConstructor
public class TimeNestServiceImpl extends ServiceImpl<TimeNestMapper, TimeNest> implements TimeNestService {

    private final NestStrategyFactory nestStrategyFactory;

    private final OssService ossService;

    private final NotificationService notificationService;

    private final UserService userService;

    private final PublicTimeNestService publicTimeNestService;

    private final UserLikesService userLikesService;

    @Override
    public List<TimeNestBo> queryMyUnlockingNestList() {
        long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<TimeNest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeNest::getUserId, userId);
        wrapper.eq(TimeNest::getUnlockedStatus, UnlockedStatusEnum.LOCK.getCode());
        wrapper.eq(TimeNest::getNestStatus, StatusEnum.NORMAL.getCode());
        wrapper.eq(TimeNest::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        // 根据解锁日期排序
        wrapper.orderByAsc(TimeNest::getUnlockTime);
        // 限制查询数量
        wrapper.last("limit " + BusinessConstant.MAX_UNLOCKING_NEST_COUNT);

        List<TimeNest> timeNestList = list(wrapper);
        if (CollectionUtils.isEmpty(timeNestList)) {
            return Collections.emptyList();
        }

        Map<Long, Integer> likeMap = queryIsLike(userId);

        // 转成boList并设置点赞标识与解锁天数
        List<TimeNestBo> timeNestBoList = TimeNestConverter.INSTANCE.timeNestListToTimeNestBoList(timeNestList);
        for (TimeNestBo timeNestBo : timeNestBoList) {
            // 计算还有几天解锁
            int unlockDays = TimeUtil.calculateUnlockDays(timeNestBo.getUnlockTime());
            timeNestBo.setUnlockDays(unlockDays);
            timeNestBo.setIsLike(likeMap.getOrDefault(timeNestBo.getId(), BusinessConstant.NOT_LIKE_FLAG));
        }

        return timeNestBoList;
    }

    @Override
    public void unlockNest(Long nestId) {
        // 先拿到这个nest
        TimeNest timeNest = getById(nestId);
        if (timeNest == null) {
            throw new BusinessException("拾光纪不存在或已被删除");
        }

        // 设置解锁信息
        timeNest.setUnlockedStatus(UnlockedStatusEnum.UNLOCK.getCode());
        Integer publicStatus = timeNest.getPublicStatus();
        if (publicStatus == PublicStatusEnum.PUBLIC.getCode()) {
            // 往公开表中添加数据
            publicTimeNestService.savePublic(nestId);
            timeNest.setPublicTime(new Date());
        }

        updateById(timeNest);

        // 根据不同的type，执行不同的策略
        Integer capsuleType = timeNest.getNestType();
        NestStrategy strategy = nestStrategyFactory.getStrategy(capsuleType);
        strategy.unlockTimeNest(timeNest);

        // 获取通知好友列表
        String unlockToUserIds = timeNest.getUnlockToUserIds();
        List<Long> userIdList = JSON.parseObject(unlockToUserIds, new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(userIdList)) {
            return;
        }

        // 记录解锁通知
        notificationService.recordUnlockNotice(userIdList, nestId);
    }

    @Override
    public void createTimeNest(TimeNestBo timeNestBo) {
        TimeNest timeNest = TimeNestConverter.INSTANCE.timeNestBoToTimeNest(timeNestBo);
        timeNest.setUserId(StpUtil.getLoginIdAsLong());

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

        // 执行对应的策略,完成个性化的创建
        Integer nestType = timeNestBo.getNestType();
        NestStrategy strategy = nestStrategyFactory.getStrategy(nestType);
        timeNest = strategy.createTimeNest(timeNest);
        save(timeNest);
    }

    @Override
    public String uploadImageNest(MultipartFile file) throws IOException {
        return ossService.uploadImageNest(file);
    }

    @Override
    public Page<TimeNestBo> queryMyTimeNestList(TimeNestDto timeNestDto) {
        long currentUsersId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<TimeNest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeNest::getUserId, currentUsersId);
        wrapper.eq(TimeNest::getNestStatus, StatusEnum.NORMAL.getCode());
        wrapper.eq(TimeNest::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        wrapper.orderByAsc(TimeNest::getCreatedAt);
        if (timeNestDto.getNestType() != null) {
            wrapper.eq(TimeNest::getNestType, timeNestDto.getNestType());
        }
        if (timeNestDto.getUnlockedStatus() != null) {
            wrapper.eq(TimeNest::getUnlockedStatus, timeNestDto.getUnlockedStatus());
        }
        Page<TimeNest> timeNestPage = new Page<>(timeNestDto.getPageNum(), timeNestDto.getPageSize());
        // 分页查询
        timeNestPage = baseMapper.selectPage(timeNestPage, wrapper);
        List<TimeNest> timeNestList = timeNestPage.getRecords();

        Map<Long, Integer> likeMap = queryIsLike(currentUsersId);

        // 转成bo并设置是否点赞标识
        List<TimeNestBo> timeNestBoList = TimeNestConverter.INSTANCE.timeNestListToTimeNestBoList(timeNestList);
        for (TimeNestBo timeNestBo : timeNestBoList) {
            timeNestBo.setIsLike(likeMap.getOrDefault(timeNestBo.getId(), BusinessConstant.NOT_LIKE_FLAG));
        }

        return new Page<TimeNestBo>(timeNestPage.getCurrent(), timeNestPage.getSize(), timeNestPage.getTotal()).setRecords(timeNestBoList);
    }

    @Override
    public TimeNestBo queryTimeNest(Long id) {
        TimeNest timeNest = getById(id);
        // 基础校验
        if (basicCheck(timeNest)) {
            return null;
        }

        // 转成bo
        TimeNestBo timeNestBo = TimeNestConverter.INSTANCE.timeNestToTimeNestBo(timeNest);
        List<Long> userIdList = JSON.parseObject(timeNest.getFriendIds(), new TypeReference<>() {
        });
        userIdList.add(timeNest.getUserId());
        timeNestBo.setFriendIdList(userIdList);

        // 拿到一同创建的好友信息
        List<UsersBo> usersBoList = userService.getUsersBoList(userIdList);
        timeNestBo.setTogetherUsers(usersBoList);

        // 设置一些空返回
        return setNull(timeNestBo);
    }

    @Override
    public Page<TimeNestBo> queryPublicTimeNestList(TimeNestDto timeNestDto) {
        long currentUserId = StpUtil.getLoginIdAsLong();

        Page<PublicTimeNest> publicTimeNestPage = new Page<>(timeNestDto.getPageNum(), timeNestDto.getPageSize());
        // 分页查询
        publicTimeNestPage = publicTimeNestService.selectPage(publicTimeNestPage);
        List<PublicTimeNest> publicTimeNestList = publicTimeNestPage.getRecords();
        List<Long> timeNestIdList = publicTimeNestList.stream().map(PublicTimeNest::getTimeNestId).collect(Collectors.toList());
        Page<TimeNestBo> timeNestBoPage = new Page<>();
        timeNestBoPage.setCurrent(publicTimeNestPage.getCurrent());
        timeNestBoPage.setTotal(publicTimeNestPage.getTotal());
        timeNestBoPage.setSize(publicTimeNestPage.getSize());
        timeNestBoPage.setPages(publicTimeNestPage.getPages());
        if (CollectionUtils.isEmpty(timeNestIdList)) {
            return timeNestBoPage;
        }

        // 目前不能异步的去做，同步来做
        Map<Long, Integer> likeMap = queryIsLike(currentUserId);

        // 根据timeNestIdList查询TimeNest
        LambdaQueryWrapper<TimeNest> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TimeNest::getId, timeNestIdList);
        wrapper.eq(TimeNest::getNestStatus, StatusEnum.NORMAL.getCode());
        wrapper.eq(TimeNest::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        wrapper.orderByAsc(TimeNest::getUpdatedAt);
        List<TimeNest> timeNestList = list(wrapper);

        List<TimeNestBo> timeNestBoList = TimeNestConverter.INSTANCE.timeNestListToTimeNestBoList(timeNestList);

        // 遍历后设置是否点赞
        for (TimeNestBo timeNestBo : timeNestBoList) {
            timeNestBo.setIsLike(likeMap.getOrDefault(timeNestBo.getId(), BusinessConstant.NOT_LIKE_FLAG));
        }

        timeNestBoPage.setRecords(timeNestBoList);
        return timeNestBoPage;
    }

    @Override
    public Page<TimeNestBo> queryMyLikeTimeNestList(TimeNestDto timeNestDto) {
        long currentUserId = StpUtil.getLoginIdAsLong();
        Page<UserLikes> userLikesPage = userLikesService.queryMyLikeTimeNestList(timeNestDto, currentUserId);
        List<Long> timeNestIdList = userLikesPage.getRecords().stream().map(UserLikes::getTimeNestId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(timeNestIdList)) {
            return new Page<>();
        }
        Map<Long, Integer> likeMap = queryIsLike(currentUserId);

        // 根据timeNestIdList查询TimeNest
        LambdaQueryWrapper<TimeNest> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TimeNest::getId, timeNestIdList);
        wrapper.eq(TimeNest::getNestStatus, StatusEnum.NORMAL.getCode());
        wrapper.eq(TimeNest::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        wrapper.orderByAsc(TimeNest::getUpdatedAt);
        List<TimeNest> timeNestList = list(wrapper);

        List<TimeNestBo> timeNestBoList = TimeNestConverter.INSTANCE.timeNestListToTimeNestBoList(timeNestList);

        // 遍历后设置是否点赞
        for (TimeNestBo timeNestBo : timeNestBoList) {
            timeNestBo.setIsLike(likeMap.getOrDefault(timeNestBo.getId(), BusinessConstant.NOT_LIKE_FLAG));
        }

        Page<TimeNestBo> timeNestBoPage = new Page<TimeNestBo>(userLikesPage.getCurrent(), userLikesPage.getSize(), userLikesPage.getTotal()).setRecords(timeNestBoList);
        timeNestBoPage.setRecords(timeNestBoList);
        return timeNestBoPage;
    }

    // 查询这个用户点赞的拾光纪
    private Map<Long, Integer> queryIsLike(long currentUserId) {
        LambdaQueryWrapper<UserLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLikes::getUserId, currentUserId);
        wrapper.eq(UserLikes::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        List<UserLikes> userLikes = userLikesService.list(wrapper);
        if (!CollectionUtils.isEmpty(userLikes)) {
            return userLikes.stream().collect(Collectors.toMap(UserLikes::getTimeNestId, userLikes1 -> BusinessConstant.LIKE_FLAG));
        }
        return new HashMap<>();
    }

    // 设置一些不必要的返回信息
    private TimeNestBo setNull(TimeNestBo timeNestBo) {
        timeNestBo.setId(null);
        timeNestBo.setUserId(null);
        for (UsersBo togetherUser : timeNestBo.getTogetherUsers()) {
            togetherUser.setEmail(null);
            togetherUser.setPhone(null);
        }
        timeNestBo.setUnlockTime(null);
        timeNestBo.setPublicTime(null);
        timeNestBo.setCreatedAt(null);
        return timeNestBo;
    }

    /**
     * 基础校验
     *
     * @param timeNest 拾光纪实体
     * @return true-不可访问，false-可访问
     */
    private boolean basicCheck(TimeNest timeNest) {
        if (Objects.isNull(timeNest)) {
            throw new BusinessException("拾光纪不存在或已被删除");
        }

        long currentUserId = StpUtil.getLoginIdAsLong();
        boolean isOwner = timeNest.getUserId() == currentUserId;
        boolean isUnlocked = timeNest.getUnlockedStatus() == UnlockedStatusEnum.UNLOCK.getCode();
        boolean isPublic = timeNest.getPublicStatus() == PublicStatusEnum.PUBLIC.getCode();

        // 当前拾光纪还未解锁
        if (!isUnlocked) {
            return true;
        }

        // 不是我创建的拾光纪，且还未公开，那么不可见！
        if (!isOwner && !isPublic) {
            throw new BusinessException("无权访问该拾光纪");
        }

        // 不是我的，公开了，但未到公开时间，那么不可见！
        return !isOwner && timeNest.getPublicTime() != null && timeNest.getPublicTime().after(new Date());
    }

}
