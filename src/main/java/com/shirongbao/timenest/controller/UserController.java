package com.shirongbao.timenest.controller;

import com.shirongbao.timenest.common.Result;
import com.shirongbao.timenest.converter.UserConverter;
import com.shirongbao.timenest.pojo.bo.UsersBo;
import com.shirongbao.timenest.pojo.dto.UsersDto;
import com.shirongbao.timenest.pojo.vo.UsersVo;
import com.shirongbao.timenest.service.EmailService;
import com.shirongbao.timenest.service.UserService;
import com.shirongbao.timenest.validation.RegisterValidation;
import com.shirongbao.timenest.validation.SentFriendRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    // 注册
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated(RegisterValidation.class) UsersDto request) {
        return userService.register(request);
    }

    // 发送邮箱验证码
    @GetMapping("/sendEmailCode")
    public Result<String> sendEmailCode(@RequestParam("email") String email) {
        return emailService.sendEmailCode(email);
    }

    // 登录
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated UsersDto request) {
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
        try {
            UsersBo usersBo = UserConverter.INSTANCE.usersDtoToUsersBo(usersDto);
            return userService.sendFriendRequest(usersBo);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

}
