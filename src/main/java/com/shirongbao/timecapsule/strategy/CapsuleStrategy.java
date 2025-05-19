package com.shirongbao.timecapsule.strategy;

import com.shirongbao.timecapsule.pojo.entity.Capsule;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description:
 */
public interface CapsuleStrategy {

    int getCode();

    void unlockCapsule(Capsule capsule);
}
