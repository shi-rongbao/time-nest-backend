package com.shirongbao.timecapsule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timecapsule.pojo.bo.CapsuleBo;
import com.shirongbao.timecapsule.pojo.entity.Capsule;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description:
 */
public interface CapsuleService extends IService<Capsule> {

    // 查询当前用户快要解锁的capsule（最多4个）
    List<CapsuleBo> queryMyUnlockingCapsuleList();

    // 解锁capsule
    void unlockCapsule(Long capsuleId);
}
