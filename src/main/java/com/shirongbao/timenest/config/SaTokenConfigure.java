package com.shirongbao.timenest.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: ShiRongbao
 * @date: 2025-05-16
 * @description: sa-token配置拦截器
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/test/**",
                        "/user/sendEmailCode",
                        "/user/login",
                        "/wx/login/**",
                        "/user/register",
                        "/error");
    }

}
