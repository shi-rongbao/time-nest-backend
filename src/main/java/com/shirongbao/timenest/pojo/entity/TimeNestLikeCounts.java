package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 时光巢点赞数统计
 */
@Data
public class TimeNestLikeCounts {

    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // nest的id (关联 time_nest.id)
    private Long timeNestId;

    // 点赞总数
    private Long likeCount;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;
}