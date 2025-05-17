package com.shirongbao.timecapsule;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description:
 */
@SpringBootApplication
@MapperScan("com.shirongbao.timecapsule.dao")
public class TimeCapsuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeCapsuleApplication.class, args);
    }

}
