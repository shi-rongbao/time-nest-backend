package com.shirongbao.timenest.controller;

import com.shirongbao.timenest.common.Result;
import com.shirongbao.timenest.converter.TimeNestConverter;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.dto.TimeNestDto;
import com.shirongbao.timenest.pojo.vo.TimeNestVo;
import com.shirongbao.timenest.service.TimeNestService;
import com.shirongbao.timenest.validation.UnlockNestValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-17
 * @description: 拾光纪条目接口控制器
 */
@RestController
@RequestMapping("/timeNest")
@RequiredArgsConstructor
public class TimeNestController {

    private final TimeNestService timeNestService;

    // 查询“我”快要解锁的拾光纪条目列表（最多4个）
    @GetMapping("/queryMyUnlockingNestList")
    public Result<List<TimeNestVo>> queryMyUnlockingNestList() {
        try {
            List<TimeNestBo> timeNestBoList = timeNestService.queryMyUnlockingNestList();
            List<TimeNestVo> timeNestVoList = TimeNestConverter.INSTANCE.tineNestBoListToTimeNestVoList(timeNestBoList);
            return Result.success(timeNestVoList);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 提前解锁nest
    @PostMapping("/unlockNest")
    public Result<Boolean> unlockNest(@RequestBody @Validated(UnlockNestValidation.class) TimeNestDto timeNestDto) {
        try {
            Long nestId = timeNestDto.getId();
            timeNestService.unlockNest(nestId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // 创建nest
    @PostMapping("/createTimeNest")
    public Result createTimeNest(@RequestBody @Validated TimeNestDto timeNestDto) {
        TimeNestBo timeNestBo = TimeNestConverter.INSTANCE.timeNestDtoToTimeNestBo(timeNestDto);
        timeNestService.createTimeNest(timeNestBo);
        return Result.success();
    }

}
