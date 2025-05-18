package com.shirongbao.timecapsule.controller;

import com.shirongbao.timecapsule.common.Result;
import com.shirongbao.timecapsule.converter.CapsuleConverter;
import com.shirongbao.timecapsule.pojo.bo.CapsuleBo;
import com.shirongbao.timecapsule.pojo.vo.CapsuleVo;
import com.shirongbao.timecapsule.service.CapsuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-17
 * @description: 胶囊接口控制器
 */
@RestController
@RequestMapping("/capsule")
@RequiredArgsConstructor
public class CapsuleController {

    private final CapsuleService capsuleService;

    // 查询“我”快要解锁的capsule列表（最多4个）
    @GetMapping("/queryMyUnlockingCapsuleList")
    public Result<List<CapsuleVo>> queryMyUnlockingCapsuleList() {
        try {
            List<CapsuleBo> capsuleBoList = capsuleService.queryMyUnlockingCapsuleList();
            List<CapsuleVo> capsuleVoList = CapsuleConverter.INSTANCE.capsuleBoListToCapsuleVoList(capsuleBoList);
            return Result.success(capsuleVoList);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

}
