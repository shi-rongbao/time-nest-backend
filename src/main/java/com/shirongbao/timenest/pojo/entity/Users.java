package com.shirongbao.timenest.pojo.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-07-17
 * @description: 用户实体类
 */
@Data
public class Users {
    // 主键
    private Long id;

    // 唯一不重复用户名
    private String userAccount;

    // 微信open_id
    private String openId;

    // 用户昵称
    private String nickName;

    // 手机号（加密）
    private String phone;

    // 邮箱
    private String email;

    // 密码（加密）
    private String password;

    // 用户头像url
    private String avatarUrl;

    // 状态：1-正常；0-禁用
    private Integer status;

    // 用户简介
    private String introduce;

    // 注销申请：1-已申请；0-未申请
    private Integer deactivationRequested;

    // 申请注销时间
    private Date deactivationRequestedTime;

    // 逻辑删除：1-已删除；0-未删除
    private Integer isDeleted;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;
}