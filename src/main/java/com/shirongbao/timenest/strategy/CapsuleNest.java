package com.shirongbao.timenest.strategy;

import com.shirongbao.timenest.common.enums.NestTypeEnum;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import org.springframework.stereotype.Component;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 胶囊nest
 */
@Component
public class CapsuleNest implements NestStrategy {

    @Override
    public int getCode() {
        return NestTypeEnum.CAPSULE.getCode();
    }

    @Override
    public void unlockNest(TimeNest timeNest) {
        // todo 这里通知解锁后提醒谁看
    }

}
