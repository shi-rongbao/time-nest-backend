package com.shirongbao.timenest.pojo.bo;

import lombok.Data;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 用户bo类
 */
@Data
public class UsersBo {

    // 用户账号
    private String userAccount;

    // 用户密码
    private String password;

    // 用户头像url
    private String avatarUrl;

    // 用户邮箱
    private String email;

    // 验证码
    private String verifyCode;

    // 昵称
    private String nickName;

    // 手机号
    private String phone;

    // 个人简介
    private String introduce;

    // 好友请求消息
    private String requestMessage;

}
