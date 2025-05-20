package com.shirongbao.timenest.strategy;

import com.shirongbao.timenest.common.enums.NestTypeEnum;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import org.springframework.stereotype.Component;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 邮件nest
 */
@Component
public class MailNest implements NestStrategy {

    @Override
    public int getCode() {
        return NestTypeEnum.MAIL.getCode();
    }

    @Override
    public void unlockTimeNest(TimeNest timeNest) {
        // todo 发送邮件
    }

    @Override
    public TimeNest createTimeNest(TimeNest timeNest) {

        return null;
    }

}
