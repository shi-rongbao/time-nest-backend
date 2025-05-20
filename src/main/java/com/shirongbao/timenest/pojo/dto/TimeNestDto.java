package com.shirongbao.timenest.pojo.dto;

import com.shirongbao.timenest.validation.CreateNestValidation;
import com.shirongbao.timenest.validation.UnlockNestValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 拾光纪Dto类
 */
@Data
public class TimeNestDto {

    // 拾光纪条目id
    @NotNull(message = "nestId不能为空", groups = {UnlockNestValidation.class})
    private Long id;

    // 拾光纪条目类型 1-胶囊；2-邮箱；3-图片
    @NotNull(message = "nest类型不能为空", groups = {CreateNestValidation.class})
    private Integer nestType;

    // 要发送用户的邮箱（邮件类型使用）
    private String toEmail;

    // 拾光纪条目标题
    @NotBlank(message = "nest标题不能为空", groups = {CreateNestValidation.class})
    private String nestTitle;

    // 拾光纪条目内容
    @NotBlank(message = "nest内容不能为空", groups = {CreateNestValidation.class})
    private String nestContent;

    // 公开状态
    @NotNull(message = "公开状态不能为空", groups = {CreateNestValidation.class})
    private Integer publicStatus;

    // 邀请共同创建好友id
    private List<Long> friendIdList;

    // 解锁通知谁看
    private List<Long> unlockToUserIdList;

    // 解锁时间
    private Date unlockTime;

}
