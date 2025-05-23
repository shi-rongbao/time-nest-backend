package com.shirongbao.timenest.strategy;

import com.shirongbao.timenest.common.enums.NestTypeEnum;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import com.shirongbao.timenest.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 邮件nest
 */
@Component
@RequiredArgsConstructor
public class MailNest implements NestStrategy {

    private final EmailService emailService;

    @Override
    public int getCode() {
        return NestTypeEnum.MAIL.getCode();
    }

    @Override
    public void unlockTimeNest(TimeNest timeNest) {
        emailService.sendTimeNestEmail(timeNest.getToEmail(), timeNest.getNestTitle(), timeNest.getNestContent());
    }

    @Override
    public TimeNest createTimeNest(TimeNest timeNest) {
        String email = timeNest.getToEmail();
        if (StringUtils.isBlank(email)) {
            throw new RuntimeException("创建拾光纪条目异常，选择邮件类型时邮箱不能为空！");
        }
        timeNest.setToEmail(email);
        return timeNest;
    }

}
