package com.shirongbao.timenest.strategy;

import com.shirongbao.timenest.common.enums.NestTypeEnum;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import org.springframework.stereotype.Component;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 图片nest
 */
@Component
public class ImageNest implements NestStrategy {

    @Override
    public int getCode() {
        return NestTypeEnum.IMAGE.getCode();
    }

    @Override
    public void unlockTimeNest(TimeNest timeNest) {
        // todo 图片好像不用有特殊操作！
    }

    @Override
    public TimeNest createTimeNest(TimeNestBo timeNestBo) {
        return null;
    }

}
