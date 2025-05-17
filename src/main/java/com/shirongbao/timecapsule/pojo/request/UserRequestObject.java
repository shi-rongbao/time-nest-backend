package com.shirongbao.timecapsule.pojo.request;

import com.shirongbao.timecapsule.validation.RegisterValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 入参用户请求实体类
 */
@Data
public class UserRequestObject {

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{5,18}$", message = "用户名不符合格式要求！")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "邮箱不能为空", groups = RegisterValidation.class)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式不正确！")
    private String email;

    @NotBlank(message = "验证码不能为空",groups = RegisterValidation.class)
    private String verifyCode;

    // 昵称
    private String nickName;

    // 手机号
    private String phone;

    // 个人简介
    private String introduce;

}
