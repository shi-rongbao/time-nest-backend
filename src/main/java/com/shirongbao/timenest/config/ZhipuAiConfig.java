package com.shirongbao.timenest.config;

import ai.z.openapi.ZhipuAiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: ShiRongbao
 * @date: 2025-09-30
 * @description:
 */
@Configuration
public class ZhipuAiConfig {

    @Bean
    public ZhipuAiClient zhipuAiClient() {
        return ZhipuAiClient.builder()
                .apiKey("dbf9ac81b39f4c25a7c7bbd2ffcf447f.HU42PwEaRDJSnYJi")
                .build();
    }

}
