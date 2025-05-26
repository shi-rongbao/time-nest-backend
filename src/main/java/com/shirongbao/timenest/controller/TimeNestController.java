package com.shirongbao.timenest.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.converter.TimeNestConverter;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.dto.TimeNestDto;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import com.shirongbao.timenest.pojo.vo.TimeNestVo;
import com.shirongbao.timenest.service.TimeNestService;
import com.shirongbao.timenest.validation.CreateNestValidation;
import com.shirongbao.timenest.validation.TimeNestIdValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // 查询“我”快要解锁的拾光纪条目列表（最多6个）
    @GetMapping("/queryMyUnlockingNestList")
    public Result<List<TimeNestVo>> queryMyUnlockingNestList() {
        List<TimeNestBo> timeNestBoList = timeNestService.queryMyUnlockingNestList();
        List<TimeNestVo> timeNestVoList = TimeNestConverter.INSTANCE.timeNestBoListToTimeNestVoList(timeNestBoList);
        return Result.success(timeNestVoList);
    }

    // 提前解锁nest
    @PostMapping("/unlockNest")
    public Result<Boolean> unlockNest(@RequestBody @Validated(TimeNestIdValidation.class) TimeNestDto timeNestDto) {
        Long nestId = timeNestDto.getId();
        timeNestService.unlockNest(nestId);
        return Result.success();
    }

    // 创建nest
    @PostMapping("/createTimeNest")
    public Result<Boolean> createTimeNest(@RequestBody @Validated(CreateNestValidation.class) TimeNestDto timeNestDto) {
        TimeNestBo timeNestBo = TimeNestConverter.INSTANCE.timeNestDtoToTimeNestBo(timeNestDto);
        timeNestService.createTimeNest(timeNestBo);
        return Result.success();
    }

    // 上传图片nest
    @PostMapping("/uploadImageNest")
    public Result<String> uploadImageNest(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = timeNestService.uploadImageNest(file);
        return Result.success(imageUrl);
    }

    // 分页查看“我”创建的拾光纪条目列表
    @PostMapping("/queryMyTimeNestList")
    public Result<Page<TimeNest>> queryMyTimeNestList(@RequestBody TimeNestDto timeNestDto) {
        Page<TimeNest> timeNestPage = timeNestService.queryMyTimeNestList(timeNestDto);
        return Result.success(timeNestPage);
    }

    // 查看拾光纪条目
    @PostMapping("/queryTimeNest")
    public Result<TimeNestVo> queryTimeNest(@RequestBody @Validated(TimeNestIdValidation.class) TimeNestDto timeNestDto) {
        TimeNestBo timeNestBo = timeNestService.queryTimeNest(timeNestDto.getId());
        if (timeNestBo == null) {
            return Result.fail("当前拾光纪还未解锁/还未到公开时间，暂时不能查看~");
        }
        TimeNestVo timeNestVo = TimeNestConverter.INSTANCE.timeNestBoToTimeNestVo(timeNestBo);
        return Result.success(timeNestVo);
    }

    // 分页查看公开的拾光纪条目
    @PostMapping("/queryPublicTimeNestList")
    public Result<Page<TimeNestVo>> queryPublicTimeNestList(@RequestBody TimeNestDto timeNestDto) {
        Page<TimeNest> timeNestPage = timeNestService.queryPublicTimeNestList(timeNestDto);
        List<TimeNest> timeNestList = timeNestPage.getRecords();
        List<TimeNestVo> timeNestVoList = TimeNestConverter.INSTANCE.timeNestListToTimeNestVoList(timeNestList);
        Page<TimeNestVo> timeNestVoPage = new Page<>(timeNestPage.getCurrent(), timeNestPage.getSize(), timeNestPage.getTotal());
        timeNestVoPage.setRecords(timeNestVoList);
        return Result.success(timeNestVoPage);
    }

}
