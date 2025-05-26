package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 用户点赞记录
 */
@Data
public class UserLikes {

    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 点赞的用户id (关联 users.id)
    private Long userId;

    // 被点赞的nest的id (关联 time_nest.id)
    private Long timeNestId;

    // 逻辑删除(取消点赞)：1-已取消；0-未取消(即已点赞)
    private Integer isDeleted;

    // 点赞时间
    private Date createdAt;

    // 更新时间 (例如取消点赞或重新点赞时间)
    private Date updatedAt;
}