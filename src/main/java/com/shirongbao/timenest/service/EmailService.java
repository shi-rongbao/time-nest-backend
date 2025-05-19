package com.shirongbao.timenest.service;

import com.shirongbao.timenest.common.Result;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 邮箱服务接口
 */
public interface EmailService {

    // 发送验证码
    Result<String> sendEmailCode(String email);

}
