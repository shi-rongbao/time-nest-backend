package com.shirongbao.timenest.service.nest;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.entity.TimeNestLikeCounts;

/**
 * @author: ShiRongbao
 * @date: 2025-05-26
 * @description: 拾光纪点赞数服务接口
 */
public interface TimeNestLikeCountsService extends IService<TimeNestLikeCounts> {

    // 点赞（取消点赞）拾光纪
    void likeTimeNest(Long nestId, Integer likeType);

}
