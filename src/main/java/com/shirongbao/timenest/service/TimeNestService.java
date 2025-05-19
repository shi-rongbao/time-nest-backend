package com.shirongbao.timenest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.entity.TimeNest;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description:
 */
public interface TimeNestService extends IService<TimeNest> {

    // 查询当前用户快要解锁的nest（最多4个）
    List<TimeNestBo> queryMyUnlockingNestList();

    // 解锁nest
    void unlockNest(Long nestId);
}
