package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-24
 * @description: 点赞的拾光纪条目
 */
@Data
public class LikeTimeNest {

    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 拾光纪id
    private Long timeNestId;

    // 用户id
    private Long userId;

    // 点赞数
    private Long likeCount;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;

}
