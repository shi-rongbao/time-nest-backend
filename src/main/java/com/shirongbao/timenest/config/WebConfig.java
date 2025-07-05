package com.shirongbao.timenest.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shirongbao.timenest.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: Web MVC 配置类
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    private final ObjectMapper objectMapper;

    /**
     * 配置消息转换器，使用统一的 ObjectMapper
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建新的 ObjectMapper 实例用于 HTTP 消息转换，配置不序列化 null 值
        ObjectMapper httpObjectMapper = objectMapper.copy();
        httpObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        httpObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(httpObjectMapper);
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        converters.add(0, converter);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/error", "/favicon.ico");
    }
}
