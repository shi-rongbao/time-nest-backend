package com.shirongbao.timecapsule.strategy;

import com.shirongbao.timecapsule.common.enums.CapsuleTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 胶囊capsule
 */
@Component
public class Capsule implements CapsuleStrategy{

    @Override
    public int getCode() {
        return CapsuleTypeEnum.CAPSULE.getCode();
    }

    @Override
    public void unlockCapsule(com.shirongbao.timecapsule.pojo.entity.Capsule capsule) {
        // todo 这里通知解锁后提醒谁看
    }

}
