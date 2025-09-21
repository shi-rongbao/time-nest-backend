package com.shirongbao.timenest.service.email.impl;

import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.service.email.EmailService;
import com.shirongbao.timenest.utils.RedisUtil;
import com.shirongbao.timenest.utils.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 邮箱服务实现类
 */
@Slf4j // 建议添加日志注解，用于记录异常
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

    /**
     * 发送包含精美样式的“时光胶囊”邮件
     * @param toEmail 收件人邮箱
     * @param nestTitle 胶囊标题
     * @param nestContent 胶囊内容
     */
    @Override
    public void sendTimeNestEmail(String toEmail, String nestTitle, String nestContent) {
        // 1. 创建一个 MimeMessage 对象，它支持复杂的邮件内容，如HTML和附件
        MimeMessage message = mailSender.createMimeMessage();

        try {
            // 2. 使用 MimeMessageHelper 来构建邮件内容
            // 参数 true 表示允许 multipart（即HTML、文本、附件等的混合）
            // "UTF-8" 指定了编码，防止中文乱码
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 3. 设置邮件的基本信息
            helper.setFrom("拾光纪 <2502260933@qq.com>"); // 设置发件人名称，让邮件看起来更正式
            helper.setTo(toEmail);
            helper.setSubject("拾光纪：一封来自过去的信 [" + nestTitle + "]");

            // 4. 读取并填充HTML模板
            String htmlContent = loadEmailTemplate("templates/email-template.html");
            htmlContent = htmlContent.replace("{{nestTitle}}", nestTitle);

            // 将用户输入的换行符 \n 替换为HTML的换行标签 <br>
            String formattedNestContent = nestContent.replace("\n", "<br>");
            htmlContent = htmlContent.replace("{{nestContent}}", formattedNestContent);

            // 5. 将处理好的HTML内容设置到邮件中
            // 第二个参数 true 必须要有，它告诉邮件客户端这是一个HTML邮件
            helper.setText(htmlContent, true);

            // 6. 发送邮件
            mailSender.send(message);
            log.info("时光胶囊邮件已成功发送至: {}", toEmail);

        } catch (MessagingException | IOException e) {
            log.error("发送时光胶囊邮件失败，收件人: {}", toEmail, e);
            // 这里可以根据业务需求决定是否向上抛出异常
            // throw new RuntimeException("发送HTML邮件失败", e);
        }
    }

    /**
     * 从类路径加载邮件模板文件内容
     * @param path resources目录下的相对路径, 例如: "templates/email-template.html"
     * @return 模板文件内容的字符串
     * @throws IOException 如果找不到文件或读取失败
     */
    private String loadEmailTemplate(String path) throws IOException {
        URL resource = this.getClass().getClassLoader().getResource(path);
        if (resource == null) {
            throw new IOException("找不到邮件模板文件: " + path);
        }
        try {
            return new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IOException("读取邮件模板文件失败: " + path, e);
        }
    }
}