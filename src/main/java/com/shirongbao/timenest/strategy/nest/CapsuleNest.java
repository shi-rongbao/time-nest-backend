package com.shirongbao.timenest.strategy.nest;

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
    public void unlockTimeNest(TimeNest timeNest) {
        // do nothing
    }

    @Override
    public TimeNest createTimeNest(TimeNest timeNest) {
        return timeNest;
    }

}
