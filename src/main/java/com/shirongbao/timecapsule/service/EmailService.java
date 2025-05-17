package com.shirongbao.timecapsule.service;

import com.shirongbao.timecapsule.common.Result;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 邮箱服务接口
 */
public interface EmailService {

    // 发送验证码
    Result<String> sendEmailCode(String email);

}
