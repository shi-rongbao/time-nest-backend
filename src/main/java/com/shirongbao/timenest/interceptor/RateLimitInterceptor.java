package com.shirongbao.timenest.interceptor;

import com.shirongbao.timenest.anno.RateLimit;
import com.shirongbao.timenest.common.entity.RateLimitInfo;
import com.shirongbao.timenest.config.GlobalRateLimitProperties;
import com.shirongbao.timenest.service.limit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;


/**
 * @author: ShiRongbao
 * @date: 2025-05-27
 * @description: IP限流拦截器
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    private final GlobalRateLimitProperties globalRateLimitProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);

        // 步骤 1: 检查全局限流
        if (globalRateLimitProperties.isEnable()) {
            boolean allowed = checkRateLimit(
                    "global_rate_limit", // 全局限流使用固定的key
                    clientIp,
                    globalRateLimitProperties.getMinute(),
                    globalRateLimitProperties.getHour(),
                    response,
                    "全局"
            );
            if (!allowed) {
                return false;
            }
        }

        // 步骤 2: 检查注解限流
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit != null && rateLimit.enableIpLimit()) {
            boolean allowed = checkRateLimit(
                    rateLimit.key(), // 注解限流使用自定义的key
                    clientIp,
                    rateLimit.minuteLimit(),
                    rateLimit.hourLimit(),
                    response,
                    "接口"
            );
            if (!allowed) {
                return false;
            }
        }

        return true;
    }

    private boolean checkRateLimit(String keyPrefix, String clientIp, int minuteLimit, int hourLimit, HttpServletResponse response, String limitType) throws IOException, IOException {
        if (!rateLimitService.isAllowed(keyPrefix, clientIp, minuteLimit, hourLimit)) {
            RateLimitInfo info = rateLimitService.getRateLimitInfo(keyPrefix, clientIp, minuteLimit, hourLimit);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");

            response.setHeader("X-RateLimit-Minute-Remaining", String.valueOf(info.getMinuteRemaining()));
            response.setHeader("X-RateLimit-Hour-Remaining", String.valueOf(info.getHourRemaining()));
            response.setHeader("X-RateLimit-Minute-Reset", String.valueOf(info.getMinuteResetTime()));
            response.setHeader("X-RateLimit-Hour-Reset", String.valueOf(info.getHourResetTime()));

            String errorMsg = String.format(
                    "{\"code\":429,\"message\":\"访问频率超出%s限制\",\"data\":{\"minuteRemaining\":%d,\"hourRemaining\":%d,\"minuteResetTime\":%d,\"hourResetTime\":%d}}",
                    limitType, info.getMinuteRemaining(), info.getHourRemaining(), info.getMinuteResetTime(), info.getHourResetTime()
            );

            response.getWriter().write(errorMsg);
            return false;
        }
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}