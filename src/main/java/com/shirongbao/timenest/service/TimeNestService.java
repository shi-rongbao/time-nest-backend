package com.shirongbao.timenest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.bo.TimeNestBo;
import com.shirongbao.timenest.pojo.dto.TimeNestDto;
import com.shirongbao.timenest.pojo.entity.TimeNest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 拾光纪服务接口
 */
public interface TimeNestService extends IService<TimeNest> {

    // 查询当前用户快要解锁的nest（最多4个）
    List<TimeNestBo> queryMyUnlockingNestList();

    // 解锁nest
    void unlockNest(Long nestId);

    // 创建拾光纪条目
    void createTimeNest(TimeNestBo timeNestBo);

    // 上传图片nest
    String uploadImageNest(MultipartFile file) throws IOException;

    // 分页查询“我”创建的拾光纪条目
    Page<TimeNestBo> queryMyTimeNestList(TimeNestDto timeNestDto);

    // 查询拾光纪条目
    TimeNestBo queryTimeNest(Long id);

    // 分页查看公开的拾光纪条目
    Page<TimeNestBo> queryPublicTimeNestList(TimeNestDto timeNestDto);

    // 分页查看“我”点赞过的拾光纪条目
    Page<TimeNestBo> queryMyLikeTimeNestList(TimeNestDto timeNestDto);

}
