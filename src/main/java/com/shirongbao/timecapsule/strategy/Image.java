package com.shirongbao.timecapsule.strategy;

import com.shirongbao.timecapsule.common.enums.CapsuleTypeEnum;
import com.shirongbao.timecapsule.pojo.entity.Capsule;
import org.springframework.stereotype.Component;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 图片capsule
 */
@Component
public class Image implements CapsuleStrategy{

    @Override
    public int getCode() {
        return CapsuleTypeEnum.IMAGE.getCode();
    }

    @Override
    public void unlockCapsule(Capsule capsule) {
        // todo 图片好像不用有特殊操作！
    }

}
