package com.shirongbao.timenest.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.common.Result;
import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.common.enums.*;
import com.shirongbao.timenest.converter.FriendshipsConverter;
import com.shirongbao.timenest.converter.UserConverter;
import com.shirongbao.timenest.dao.UserMapper;
import com.shirongbao.timenest.pojo.bo.FriendRequestNotificationBo;
import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.entity.FriendRequestNotification;
import com.shirongbao.timenest.pojo.entity.FriendRequests;
import com.shirongbao.timenest.pojo.entity.Friendships;
import com.shirongbao.timenest.pojo.entity.Users;
import com.shirongbao.timenest.pojo.dto.UsersDto;
import com.shirongbao.timenest.pojo.vo.UsersVo;
import com.shirongbao.timenest.service.FriendRequestNotificationService;
import com.shirongbao.timenest.service.FriendRequestsService;
import com.shirongbao.timenest.service.FriendshipsService;
import com.shirongbao.timenest.service.UserService;
import com.shirongbao.timenest.service.oss.OssService;
import com.shirongbao.timenest.utils.RedisUtil;
import com.shirongbao.timenest.utils.SecurityUtil;
import com.shirongbao.timenest.utils.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 用户服务实现类
 */
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, Users> implements UserService {

    // 递归最多重试 3 次
    private static final int MAX_RETRY_TIMES = 3;

    private final RedisUtil redisUtil;

    private final OssService ossService;

    private final FriendRequestsService friendRequestsService;

    private final FriendRequestNotificationService friendRequestNotificationService;

    private final FriendshipsService friendshipsService;

    private final ThreadPoolExecutor labelThreadPool;

    @Override
    public Result<String> register(UsersDto request) {
        // 先校验验证码是否正确
        String email = request.getEmail();
        String requestVerifyCode = request.getVerifyCode();
        String key = redisUtil.buildKey(RedisConstant.EMAIL_PREFIX, email);
        if (StringUtils.isBlank(key)) {
            return Result.fail("还未获取验证码或验证码已过期，请重新获取验证码！");
        }

        String verifyCode = redisUtil.get(key).toString();
        if (StringUtils.isBlank(verifyCode)) {
            return Result.fail("验证码已过期，请重新获取验证码！");
        }

        // 验证码不同返回失败
        if (!verifyCode.equals(VerificationCodeUtil.convertToUpperCase(requestVerifyCode))) {
            return Result.fail("验证码错误！");
        }

        // 要校验当前用户名是否已经存在
        String userAccount = request.getUserAccount();
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserAccount, userAccount);
        Users users = getOne(wrapper);
        if (ObjectUtils.isNotEmpty(users)) {
            return Result.fail("当前用户名已经存在！");
        }

        // 要校验当前邮箱是否已经为其他用户注册过
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getEmail, email);
        users = getOne(wrapper);
        if (ObjectUtils.isNotEmpty(users)) {
            return Result.fail("当前邮箱已为其他用户注册过！");
        }

        // 加密密码
        String password = request.getPassword();
        String encryptPassword = SecurityUtil.encryptPassword(password);

        // 按规则生成用户默认昵称(tc_nickname_${date})
        String nickName = "tc_nickname_" + System.currentTimeMillis();
        users = new Users();
        users.setNickName(nickName);
        users.setEmail(email);
        users.setPassword(encryptPassword);
        users.setUserAccount(userAccount);
        // 设置默认的头像
        users.setAvatarUrl("https://time-capsule-rongbao.oss-rg-china-mainland.aliyuncs.com/avatar/1c829f2997583dbe2f8338c1b5499da4.jpg");
        save(users);
        return Result.success("注册成功！");
    }

    @Override
    public String login(UsersDto request) {
        // 查询这个用户是否存在，不存在直接返回
        String userAccount = request.getUserAccount();
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserAccount, userAccount);
        wrapper.eq(Users::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        Users users = getOne(wrapper);
        if (ObjectUtils.isEmpty(users)) {
            return null;
        }

        // 校验密码是否正确
        String password = request.getPassword();
        // 从数据库中获取存储的包含盐值的哈希值
        String storedPasswordWithSalt = users.getPassword();
        String[] parts = storedPasswordWithSalt.split("\\$");
        if (parts.length != 2) {
            // 处理数据库中密码格式错误的情况，例如没有盐值或者格式不正确
            return null; // 或者抛出异常
        }
        String storedSalt = parts[0];
        String storedHash = parts[1];

        if (SecurityUtil.verifyPassword(password, storedHash, storedSalt)) {
            // 校验成功后登录并返回token
            StpUtil.login(users.getId());
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            String tokenValue = tokenInfo.getTokenValue();
            // 把用户基础信息放到缓存里
            setUserCache(tokenValue);
            // 判断是否取消注销
            cancelDeactivateRequest(users);
            return tokenValue;
        }

        return null;
    }

    @Override
    public Result<String> uploadAvatar(MultipartFile file) throws IOException {
        String url = ossService.uploadAvatar(file);
        if (StringUtils.isBlank(url)) {
            return Result.fail("头像上传失败,请稍后重试!");
        }

        // 更新用户数据库头像
        long userId = StpUtil.getLoginIdAsLong();
        Users users = getById(userId);
        users.setAvatarUrl(url);
        updateById(users);

        // 删掉用户缓存
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String tokenValue = tokenInfo.getTokenValue();
        removeUserCache(tokenValue);
        return Result.success(url);
    }

    @Override
    public UsersVo getUserInfo() {
        return getUserInfoWithRetry(0);
    }

    @Override
    public Result<Boolean> updateUserInfo(UsersDto request) {
        long userId = StpUtil.getLoginIdAsLong();
        Users users = UserConverter.INSTANCE.requestObjectToEntity(request);
        users.setId(userId);

        // 如果手机号不为空，加密后存储
        String phone = request.getPhone();
        if (StringUtils.isNotBlank(phone)) {
            String encryptedPhone = SecurityUtil.encryptPhoneNumber(phone);
            users.setPhone(encryptedPhone);
        }

        // 如果密码不为空，加密后存储
        String password = request.getPassword();
        if (StringUtils.isNotBlank(password)) {
            String encryptedPassword = SecurityUtil.encryptPassword(password);
            users.setPassword(encryptedPassword);
        }

        // 更新用户信息
        updateById(users);

        // 删掉用户缓存
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String tokenValue = tokenInfo.getTokenValue();
        removeUserCache(tokenValue);
        return Result.success();
    }

    @Override
    public void logout() {
        StpUtil.logout();
        // 把当前token的缓存移除
        removeUserCache(StpUtil.getTokenInfo().getTokenValue());
    }

    @Override
    public void deactivateRequest() {
        long userId = StpUtil.getLoginIdAsLong();
        Users users = getById(userId);
        // 设置15天的注销冷静期
        users.setDeactivationRequested(DeactivationRequestedEnum.YES.getCode());
        users.setDeactivationRequestedTime(DateUtils.addDays(new Date(), 15));
        updateById(users);

        // 做登出
        logout();
    }

    @Override
    public List<Users> queryAllDeactivationRequested() {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getDeactivationRequested, DeactivationRequestedEnum.YES.getCode());
        wrapper.eq(Users::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        return list(wrapper);
    }

    // 批量更新是带事物的，所以这里用@Transactional（会失效，所以手动加上）
    @Transactional
    @Override
    public void doLogicDelete(List<Users> usersList) {
        for (Users users : usersList) {
            users.setIsDeleted(IsDeletedEnum.DELETED.getCode());
        }
        updateBatchById(usersList);
    }

    @Override
    public Result<String> sendFriendRequest(UsersBo usersBo) {
        String userAccount = usersBo.getUserAccount();
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserAccount, userAccount);
        wrapper.eq(Users::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        wrapper.eq(Users::getStatus, StatusEnum.NORMAL.getCode());
        Users users = getOne(wrapper);
        if (ObjectUtils.isEmpty(users)) {
            throw new RuntimeException("当前用户账号异常，添加失败~");
        }

        // 要先判断这个用户是否是我的好友
        boolean isFriend = friendshipsService.checkIsFriend(users.getId());
        if (isFriend) {
            throw new RuntimeException("你们已经是好友啦，不需要再添加了~");
        }

        Long receiverUserId = users.getId();
        long senderUserId = StpUtil.getLoginIdAsLong();

        // 记录好友请求表
        FriendRequests friendRequests = new FriendRequests();
        friendRequests.setSenderUserId(senderUserId);
        friendRequests.setReceiverUserId(receiverUserId);
        friendRequests.setRequestMessage(usersBo.getRequestMessage());

        // 查询是否已经有未处理的请求
        LambdaQueryWrapper<FriendRequests> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendRequests::getSenderUserId, senderUserId);
        queryWrapper.eq(FriendRequests::getReceiverUserId, receiverUserId);
        queryWrapper.eq(FriendRequests::getProcessingStatus, ProcessingStatusEnum.WAITING.getCode());
        queryWrapper.eq(FriendRequests::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        FriendRequests one = friendRequestsService.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(one)) {
            return Result.success("当前已发送过好友申请,请勿再次发送!");
        }

        friendRequestsService.save(friendRequests);

        // 记录通知表
        friendRequestNotificationService.saveNotification(friendRequests.getId(), receiverUserId, senderUserId);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Boolean> processingFriendRequest(Long friendRequestId, Integer processingResult) {
        LambdaQueryWrapper<FriendRequests> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FriendRequests::getId, friendRequestId);
        queryWrapper.eq(FriendRequests::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        FriendRequests friendRequests = friendRequestsService.getOne(queryWrapper);
        if (ObjectUtils.isEmpty(friendRequests)) {
            throw new RuntimeException("当前好友请求不存在，请刷新后重试！");
        }

        friendRequests.setProcessingStatus(ProcessingStatusEnum.PASSING.getCode());
        friendRequests.setProcessingResult(ProcessingResultEnum.REFUSE.getCode());

        if (processingResult == ProcessingResultEnum.ACCEPT.getCode()) {
            friendRequests.setProcessingResult(ProcessingResultEnum.ACCEPT.getCode());

            // 往好友表里插入数据
            Friendships friendships = new Friendships();
            Long user1Id = friendRequests.getSenderUserId();
            friendships.setUserId1(user1Id);
            Long user2Id = friendRequests.getReceiverUserId();
            friendships.setUserId2(user2Id);

            Users users1 = getById(user1Id);
            Users users2 = getById(user2Id);

            friendships.setUserAccount1(users1.getUserAccount());
            friendships.setUserAccount2(users2.getUserAccount());
            friendshipsService.save(friendships);
        }

        friendRequestsService.updateById(friendRequests);
        return Result.success();
    }

    @Override
    public List<UsersVo> getFriendList() throws ExecutionException, InterruptedException {
        long currentUserId = StpUtil.getLoginIdAsLong();
        List<Friendships> friendshipsList = friendshipsService.getFriendList(currentUserId);
        if (friendshipsList.isEmpty()) {
            return Collections.emptyList();
        }

        List<UsersVo> usersVoList = new ArrayList<>();

        // 拿到所有的futures
        List<CompletableFuture<List<UsersVo>>> futures = new ArrayList<>();
        for (Friendships friendships : friendshipsList) {
            // 异步去执行
            CompletableFuture<List<UsersVo>> future = CompletableFuture.supplyAsync(() -> {
                UsersVo usersVo = new UsersVo();
                String userAccount;
                Long userId;
                // 这里是拿到好友的账号
                if (friendships.getUserId1() == currentUserId) {
                     userAccount = friendships.getUserAccount2();
                     userId = friendships.getUserId2();
                } else {
                    userAccount = friendships.getUserAccount1();
                    userId = friendships.getUserId1();
                }
                usersVo.setUserAccount(userAccount);
                // 拿到好友的信息并返回
                Users friendUser = getById(userId);
                usersVo.setUserAccount(userAccount);
                usersVo.setNickName(friendUser.getNickName());
                usersVo.setAvatarUrl(friendUser.getAvatarUrl());
                usersVo.setIntroduce(friendUser.getIntroduce());
                usersVoList.add(usersVo);
                return usersVoList;
            }, labelThreadPool);

            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join();

        // 执行完就可以返回了
        return usersVoList;
    }

    @Override
    public List<FriendRequestNotificationBo> combineUserAccount(List<FriendRequestNotification> friendRequestNotificationList) {
        // 拿到未读通知中所有的发请求的用户id
        List<Long> sendUserIdList = friendRequestNotificationList.stream().map(FriendRequestNotification::getSenderUserId).collect(Collectors.toList());
        // 批量查询到全部用户
        LambdaQueryWrapper<Users> usersWrapper = new LambdaQueryWrapper<>();
        usersWrapper.in(Users::getId, sendUserIdList);
        usersWrapper.eq(Users::getIsDeleted, IsDeletedEnum.NOT_DELETED.getCode());
        usersWrapper.eq(Users::getStatus, StatusEnum.NORMAL.getCode());
        List<Users> usersList = list(usersWrapper);
        // 转成map，key是usersId，value是userAccount
        Map<Long, String> userAccountMap = usersList.stream().collect(Collectors.toMap(Users::getId, Users::getUserAccount));

        // 转换后组装userAccount
        List<FriendRequestNotificationBo> friendRequestNotificationBoList = FriendshipsConverter.INSTANCE.friendRequestNotificationListToFriendRequestNotificationBoList(friendRequestNotificationList);

        for (FriendRequestNotificationBo friendRequestNotificationBo : friendRequestNotificationBoList) {
            friendRequestNotificationBo.setRequestUserAccount(userAccountMap.get(friendRequestNotificationBo.getSenderUserId()));
        }

        return friendRequestNotificationBoList;
    }

    // 获取用户信息，最多执行三次递归
    private UsersVo getUserInfoWithRetry(int retryTimes) {
        if (retryTimes > MAX_RETRY_TIMES) {
            throw new RuntimeException("获取用户信息失败，请稍后再试或联系管理员！");
        }

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String tokenValue = tokenInfo.getTokenValue();
        Users users = getUserCache(tokenValue);

        // 缓存里没有，尝试重新加载缓存并重试
        if (ObjectUtils.isEmpty(users)) {
            try {
                setUserCache(tokenValue);
            } catch (Exception e) {
                // 可以记录日志，如 log.warn("设置用户缓存失败", e);
                return getUserInfoWithRetry(retryTimes + 1);
            }

            return getUserInfoWithRetry(retryTimes + 1);
        }

        if (ObjectUtils.isEmpty(users)) {
            throw new RuntimeException("当前登录状态异常，请重新登录！");
        }

        // 解密手机号返回
        String encryptPhone = users.getPhone();
        if (StringUtils.isNotBlank(encryptPhone)) {
            users.setPhone(SecurityUtil.decryptPhoneNumber(encryptPhone));
        }

        // 转换之后返回
        return UserConverter.INSTANCE.entityToResponseObject(users);
    }

    // 将用户基础信息放到缓存里
    private void setUserCache(String tokenValue) {
        String key = redisUtil.buildKey(RedisConstant.USER_CACHE_PREFIX, tokenValue);
        // 查到这个用户信息
        Users users = getById(StpUtil.getLoginIdAsLong());
        // 序列化后缓存
        String usersString = JSON.toJSONString(users);
        // 缓存30天
        redisUtil.set(key, usersString, 30 * 24 * 60 * 60);
    }

    // 将用户基础信息缓存移除
    private void removeUserCache(String tokenValue) {
        String key = redisUtil.buildKey(RedisConstant.USER_CACHE_PREFIX, tokenValue);
        redisUtil.del(key);
    }

    // 从缓存中获取用户对象
    private Users getUserCache(String tokenValue) {
        String key = redisUtil.buildKey(RedisConstant.USER_CACHE_PREFIX, tokenValue);
        Object usersObject = redisUtil.get(key);
        if (ObjectUtils.isEmpty(usersObject)) {
            return null;
        }
        String usersString = usersObject.toString();
        if (StringUtils.isNotBlank(usersString)) {
            return JSON.parseObject(usersString, Users.class);
        }
        return null;
    }


    // 取消注销申请
    private void cancelDeactivateRequest(Users users) {
        Integer deactivationRequested = users.getDeactivationRequested();
        if (deactivationRequested == null) {
            return;
        }
        if (DeactivationRequestedEnum.YES.getCode() == deactivationRequested) {
            users.setDeactivationRequested(DeactivationRequestedEnum.NO.getCode());
            updateById(users);
        }
    }

}
