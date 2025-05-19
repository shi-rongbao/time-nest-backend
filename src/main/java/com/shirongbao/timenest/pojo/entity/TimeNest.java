package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 拾光纪实体类
 */
@Data
public class TimeNest {

    // 主键id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 用户id
    private Long userId;

    // nest类型：1-胶囊；2-邮箱；3-图片；
    private Integer nestType;

    // nest标题
    private String nestTitle;

    // nest内容
    private String nestContent;

    // 是否公开：1-公开；0-私密
    private Integer publicStatus;

    // 是否解锁：1-已解锁；0-未解锁
    private Integer unlockedStatus;

    // 解锁后提醒谁看
    private String unlockToUserIds;

    // 解锁时间
    private Date unlockTime;

    // 公开时间
    private Date publicTime;

    // nest状态：1-正常；0-禁用
    private Integer nestStatus;

    // 逻辑删除：1-已删除；0-未删除
    private Integer isDeleted;

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;

}
