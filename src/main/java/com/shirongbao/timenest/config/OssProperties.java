package com.shirongbao.timenest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: ShiRongbao
 * @date: 2025-05-16
 * @description: oss配置类
 */
@ConfigurationProperties(prefix = "aliyun.oss")
@Component
@Data
public class OssProperties {

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String bucketName;

    private String domain;

    private String avatar;

    private String nest;

}
