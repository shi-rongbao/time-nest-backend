package com.shirongbao.timenest.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shirongbao.timenest.anno.RateLimit;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.converter.NotificationConverter;
import com.shirongbao.timenest.converter.UserConverter;
import com.shirongbao.timenest.domain.NotificationDomainService;
import com.shirongbao.timenest.pojo.bo.NotificationBo;
import com.shirongbao.timenest.pojo.dto.FriendRequestsDto;
import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.dto.UsersDto;
import com.shirongbao.timenest.pojo.entity.Notification;
import com.shirongbao.timenest.pojo.vo.NotificationVo;
import com.shirongbao.timenest.pojo.vo.UsersVo;
import com.shirongbao.timenest.service.email.EmailService;
import com.shirongbao.timenest.service.friend.FriendshipsService;
import com.shirongbao.timenest.service.notification.NotificationService;
import com.shirongbao.timenest.service.auth.UserService;
import com.shirongbao.timenest.validation.LoginValidation;
import com.shirongbao.timenest.validation.RegisterValidation;
import com.shirongbao.timenest.validation.SentFriendRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 用户接口控制器
 */
@RestController()
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final EmailService emailService;

    private final NotificationService notificationService;

    private final FriendshipsService friendshipsService;

    private final NotificationDomainService notificationDomainService;

    // 校验token是否有效
    @GetMapping("/validateToken")
    public Result<Boolean> validateToken() {
        return Result.success(StpUtil.isLogin());
    }

    // 注册
    @RateLimit(minuteLimit = 3, hourLimit = 20)
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated({RegisterValidation.class}) UsersDto request) {
        return userService.register(request);
    }

    // 发送邮箱验证码
    @RateLimit(minuteLimit = 3, hourLimit = 20)
    @GetMapping("/sendEmailCode")
    public Result<String> sendEmailCode(@RequestParam("email") String email) {
        return emailService.sendEmailCode(email);
    }

    // 登录
    @RateLimit(minuteLimit = 3, hourLimit = 20)
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated({LoginValidation.class}) UsersDto request) {
        String token = userService.login(request);
        if (StringUtils.isBlank(token)) {
            return Result.fail("用户名或密码输入错误！");
        }
        return Result.success(token);
    }

    // 登出
    @GetMapping("/logout")
    public Result<Boolean> logout() {
        userService.logout();
        return Result.success(true);
    }

    // 用户上传头像
    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return userService.uploadAvatar(file);
    }

    // 获取用户基本信息
    @GetMapping("/getUserInfo")
    public Result<UsersVo> getUserInfo() {
        return Result.success(userService.getUserInfo());
    }

    // 修改基本信息
    @PostMapping("/updateUserInfo")
    public Result<Boolean> updateUserInfo(@RequestBody UsersDto request) {
        return userService.updateUserInfo(request);
    }

    // 注销
    @GetMapping("/deactivateRequest")
    public Result<Boolean> deactivateRequest() {
        userService.deactivateRequest();
        return Result.success(true);
    }

    // 发送好友申请
    @PostMapping("/sendFriendRequest")
    public Result<String> sendFriendRequest(@RequestBody @Validated({SentFriendRequest.class}) UsersDto usersDto) {
        UsersBo usersBo = UserConverter.INSTANCE.usersDtoToUsersBo(usersDto);
        return friendshipsService.sendFriendRequest(usersBo);
    }

    // 获取未读通知
    @GetMapping("/getUnreadNotifications")
    public Result<List<NotificationVo>> getUnreadNotifications() {
        long currentUserId = StpUtil.getLoginIdAsLong();
        List<Notification> notificationList = notificationService.getUnreadNotifications(currentUserId);
        if (notificationList.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        // 组装notification
        List<NotificationBo> notificationBoList = notificationDomainService.combineNotification(notificationList);
        List<NotificationVo> notificationVoList = NotificationConverter.INSTANCE.notificationBoListToNotificationVoList(notificationBoList);
        return Result.success(notificationVoList);
    }

    // 标记为已读
    @GetMapping("/markAsRead")
    public Result<Boolean> markAsRead(@RequestParam("noticeId") Long noticeId) {
        // 点击消息后将消息标记为已读
        notificationService.markAsRead(noticeId);
        return Result.success(true);
    }

    // 接受/拒绝好友申请(处理好友申请)
    @PostMapping("/processingFriendRequest")
    public Result<Boolean> processingFriendRequest(@RequestBody @Validated FriendRequestsDto friendRequestsDto) {
        Long friendRequestId = friendRequestsDto.getFriendRequestId();
        Integer processingResult = friendRequestsDto.getProcessingResult();
        return friendshipsService.processingFriendRequest(friendRequestId, processingResult);
    }

    // 查看好友列表
    @GetMapping("/getFriendList")
    public Result<List<UsersVo>> getFriendList() throws ExecutionException, InterruptedException {
        long currentUserId = StpUtil.getLoginIdAsLong();
        List<UsersVo> friendList = friendshipsService.getFriendList(currentUserId);
        return Result.success(friendList);
    }

}
