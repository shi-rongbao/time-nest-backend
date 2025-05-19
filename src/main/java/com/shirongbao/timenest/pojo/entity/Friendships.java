package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 好友关系表
 */
@Data
public class Friendships {

    // 主键id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 用户一的id
    private Long userId1;

    // 用户一的账号
    private String userAccount1;

    // 用户二的id
    private Long userId2;

    // 用户二的账号
    private String userAccount2;

    // 逻辑删除：1-已删除；0-未删除
    private Integer isDeleted;

    // 建立好友的日期
    private Date establishedAt;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updateAt;

}

