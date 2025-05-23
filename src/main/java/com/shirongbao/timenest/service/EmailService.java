package com.shirongbao.timenest.service;

import com.shirongbao.timenest.common.entity.Result;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 邮箱服务接口
 */
public interface EmailService {

    // 发送验证码
    Result<String> sendEmailCode(String email);

    // 发送拾光纪邮件
    void sendTimeNestEmail(String toEmail, String nestTitle, String nestContent);
}
