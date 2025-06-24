package com.shirongbao.timenest.config.limit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "global.rate.limit")
public class GlobalRateLimitProperties {

    /**
     * 是否启用全局限流，默认为 false
     */
    private boolean enable = false;

    /**
     * 全局分钟级限制次数
     */
    private int minute = 50;

    /**
     * 全局小时级限制次数
     */
    private int hour = 300;
}