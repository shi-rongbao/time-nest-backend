package com.shirongbao.timenest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shirongbao.timenest.dao.PublicTimeNestMapper;
import com.shirongbao.timenest.pojo.entity.PublicTimeNest;
import com.shirongbao.timenest.service.PublicTimeNestService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-24
 * @description: 公开拾光纪服务实现类
 */
@Service("publicTimeNestService")
public class PublicTimeNestServiceImpl extends ServiceImpl<PublicTimeNestMapper, PublicTimeNest> implements PublicTimeNestService {

    @Override
    public void savePublic(Long nestId) {
        PublicTimeNest publicTimeNest = new PublicTimeNest();
        publicTimeNest.setTimeNestId(nestId);
        publicTimeNest.setPublicTime(new Date());

        save(publicTimeNest);
    }

}
