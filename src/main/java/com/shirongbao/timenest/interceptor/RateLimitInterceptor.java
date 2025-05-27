package com.shirongbao.timenest.interceptor;

import com.shirongbao.timenest.anno.RateLimit;
import com.shirongbao.timenest.common.entity.RateLimitInfo;
import com.shirongbao.timenest.service.limit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * @author: ShiRongbao
 * @date: 2025-05-27
 * @description: IP限流拦截器
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RateLimitService rateLimitService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否是HandlerMethod
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        
        // 没有@RateLimit注解的方法不进行限流
        if (rateLimit == null || !rateLimit.enableIpLimit()) {
            return true;
        }
        
        // 获取客户端IP
        String clientIp = getClientIp(request);
        
        // 获取限流配置
        int minuteLimit = rateLimit.minuteLimit();
        int hourLimit = rateLimit.hourLimit();
        
        // 检查是否被限流
        if (!rateLimitService.isAllowed(clientIp, minuteLimit, hourLimit)) {
            // 获取限流信息用于响应头
            RateLimitInfo info = rateLimitService.getRateLimitInfo(clientIp, minuteLimit, hourLimit);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            
            // 添加限流信息到响应头
            response.setHeader("X-RateLimit-Minute-Remaining", String.valueOf(info.getMinuteRemaining()));
            response.setHeader("X-RateLimit-Hour-Remaining", String.valueOf(info.getHourRemaining()));
            response.setHeader("X-RateLimit-Minute-Reset", String.valueOf(info.getMinuteResetTime()));
            response.setHeader("X-RateLimit-Hour-Reset", String.valueOf(info.getHourResetTime()));
            
            // 返回错误信息
            String errorMsg = String.format(
                "{\"code\":429,\"message\":\"访问频率超出限制\",\"data\":{\"minuteRemaining\":%d,\"hourRemaining\":%d,\"minuteResetTime\":%d,\"hourResetTime\":%d}}",
                info.getMinuteRemaining(), info.getHourRemaining(), info.getMinuteResetTime(), info.getHourResetTime()
            );
            
            response.getWriter().write(errorMsg);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取客户端真实IP地址
     */
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
        
        // 处理多个IP的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}