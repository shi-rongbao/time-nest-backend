package com.shirongbao.timenest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shirongbao.timenest.pojo.entity.PublicTimeNest;

/**
 * @author: ShiRongbao
 * @date: 2025-05-24
 * @description: 公开拾光纪服务类
 */
public interface PublicTimeNestService extends IService<PublicTimeNest> {

    // 记录公开拾光纪
    void savePublic(Long nestId);

}
