package com.shirongbao.timenest.service.impl;

import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.service.EmailService;
import com.shirongbao.timenest.utils.RedisUtil;
import com.shirongbao.timenest.utils.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 邮箱服务实现类
 */
@Service("emailService")
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RedisUtil redisUtil;

    private final JavaMailSender mailSender;

    @Override
    public Result<String> sendEmailCode(String email) {
        // 防刷逻辑：1分钟之内不能重复发送验证码
        String lastSendKey = redisUtil.buildKey(RedisConstant.LAST_SEND_TIME, email);
        long currentTime = System.currentTimeMillis();

        Object lastSendTimeObj = redisUtil.get(lastSendKey);
        if (lastSendTimeObj != null) {
            long lastSendTime = Long.parseLong(lastSendTimeObj.toString());
            if (currentTime - lastSendTime < 60_000) {
                return Result.fail("请勿频繁发送验证码，请1分钟后重试");
            }
        }

        // 生成验证码并存在redis里
        String verificationCode = VerificationCodeUtil.generateVerificationCode(6);
        String toUpperCase = VerificationCodeUtil.convertToUpperCase(verificationCode);
        String codeKey = redisUtil.buildKey(RedisConstant.EMAIL_PREFIX, email);
        redisUtil.set(codeKey, toUpperCase, 300);

        // 更新最后发送时间
        redisUtil.set(lastSendKey, String.valueOf(currentTime), 60);

        // 发送验证码
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2502260933@qq.com");
        message.setTo(email);
        message.setSubject("验证码");
        message.setText("您正在进行身份验证，验证码：" + verificationCode + "，有效期5分钟。如非本人操作，请忽略本邮件。");

        mailSender.send(message);

        return Result.success("验证码已发送");
    }

}
