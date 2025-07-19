package com.shirongbao.timenest.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: 雪花算法id生成配置
 */
@Configuration
public class SnowflakeConfig {

    @Value("${snowflake.worker-id}")
    private long workerId;

    @Value("${snowflake.datacenter-id}")
    private long datacenterId;

    @Bean
    public Snowflake snowflake() {
        return IdUtil.getSnowflake(workerId, datacenterId);
    }

}
