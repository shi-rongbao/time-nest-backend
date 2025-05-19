package com.shirongbao.timecapsule.strategy;

import com.shirongbao.timecapsule.common.enums.CapsuleTypeEnum;
import com.shirongbao.timecapsule.pojo.entity.Capsule;
import org.springframework.stereotype.Component;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 邮件capsule
 */
@Component
public class Mail implements CapsuleStrategy{

    @Override
    public int getCode() {
        return CapsuleTypeEnum.MAIL.getCode();
    }

    @Override
    public void unlockCapsule(Capsule capsule) {
        // todo 发送邮件
    }

}
