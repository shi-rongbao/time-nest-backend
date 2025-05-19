package com.shirongbao.timenest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 拾光记程序主启动类
 */
@SpringBootApplication
@MapperScan("com.shirongbao.timenest.dao")
@EnableScheduling
public class TimeNestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeNestApplication.class, args);
    }

}
