package com.shirongbao.timecapsule.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 胶囊vo类
 */
@Data
public class CapsuleVo {

    // 主键id
    private Long id;

    // 用户id
    private Long userId;

    // 胶囊类型：1-胶囊；2-邮箱；3-图片；
    private Integer capsuleType;

    // 胶囊标题
    private String capsuleTitle;

    // 胶囊内容
    private String capsuleContent;

    // 是否公开：1-公开；0-私密
    private Integer publicStatus;

    // 是否解锁：1-已解锁；0-未解锁
    private Integer unlockedStatus;

    // 解锁时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date unlockTime;

    // 还剩几天解锁
    private Integer unlockDays;

    // 公开时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date publicTime;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createdAt;

}
