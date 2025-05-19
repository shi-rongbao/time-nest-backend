package com.shirongbao.timenest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: ShiRongbao
 * @date: 2025-05-19
 * @description: 线程池配置
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor labelThreadPool() {
        return new ThreadPoolExecutor(
                20,  // 核心线程数
                100,  // 最大线程数
                5, // 线程存活时间
                TimeUnit.SECONDS, // 时间单位
                new LinkedBlockingQueue<>(40),  // 阻塞队列
                Executors.defaultThreadFactory(), // 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
    }

}
