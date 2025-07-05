package com.shirongbao.timenest.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 应用程序配置类
 */
@Configuration
public class AppConfig {

    /**
     * 配置全局 ObjectMapper
     *
     * @return ObjectMapper 实例
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .findAndRegisterModules();
    }
}