package com.shirongbao.timenest.pojo.dto;

import com.shirongbao.timenest.common.entity.PageInfo;
import com.shirongbao.timenest.validation.CreateNestValidation;
import com.shirongbao.timenest.validation.LikeTimeNestValidation;
import com.shirongbao.timenest.validation.TimeNestIdValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 拾光纪Dto类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TimeNestDto extends PageInfo {

    // 拾光纪条目id
    @NotNull(message = "nestId不能为空", groups = {TimeNestIdValidation.class})
    private Long id;

    // 拾光纪条目类型 1-胶囊；2-邮箱；3-图片
    @NotNull(message = "nest类型不能为空", groups = {CreateNestValidation.class})
    private Integer nestType;

    // 点赞，取消点赞（1-点赞；0-取消点赞）
    @NotNull(message = "点赞类型不能为空", groups = {LikeTimeNestValidation.class})
    private Integer likeType;

    // 要发送用户的邮箱（邮件类型使用）
    private String toEmail;

    // 图片url(图片类型使用)
    private String imageUrl;

    // 拾光纪条目标题
    @NotBlank(message = "nest标题不能为空", groups = {CreateNestValidation.class})
    private String nestTitle;

    // 拾光纪条目内容
    @NotBlank(message = "nest内容不能为空", groups = {CreateNestValidation.class})
    private String nestContent;

    // 公开状态
    @NotNull(message = "公开状态不能为空", groups = {CreateNestValidation.class})
    private Integer publicStatus;

    // 是否解锁：1-已解锁；0-未解锁
    private Integer unlockedStatus;

    // 邀请共同创建好友id
    private List<Long> friendIdList;

    // 解锁通知谁看
    private List<Long> unlockToUserIdList;

    // 解锁时间
    private Date unlockTime;

}
