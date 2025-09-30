package com.shirongbao.timenest.service.ai.impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.core.Constants;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import com.shirongbao.timenest.common.exception.BusinessException;
import com.shirongbao.timenest.service.ai.ZhipuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author: ShiRongbao
 * @date: 2025-09-30
 * @description:
 */
@Service
@RequiredArgsConstructor
public class ZhipuServiceImpl implements ZhipuService {

    private final ZhipuAiClient zhipuAiClient;

    @Value("${ai.zhipu.summary-template:请对以下内容进行总结：{input}}")
    private String summaryTemplate;

    @Override
    public String summaryInput(String input) {
        // 使用模板优化输入内容
        String formattedInput = summaryTemplate.replace("{input}", input);

        // 创建聊天完成请求
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model(Constants.ModelChatGLM4_5)
                .messages(Collections.singletonList(
                        ChatMessage.builder()
                                .role(ChatMessageRole.USER.value())
                                .content(formattedInput)
                                .build()
                ))
                .build();

        // 发送请求
        ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(request);

        // 获取回复
        if (response.isSuccess()) {
            ChatMessage reply = response.getData().getChoices().get(0).getMessage();
            return reply.getContent().toString();
        } else {
            throw new BusinessException("总结失败，请稍后再试！");
        }
    }
}

