package com.shirongbao.timenest.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(value = {BusinessException.class})
    public Result<Boolean> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    /**
     * 处理 sa-token 未登录异常
     */
    @ExceptionHandler(value = {NotLoginException.class})
    public Result<Boolean> handleNotLoginException(NotLoginException ex) {
        log.warn("用户未登录: {}", ex.getMessage());
        return Result.fail("当前账号登录状态异常，请重新登录！");
    }

    /**
     * 处理连接异常
     */
    @ExceptionHandler(value = {ConnectException.class})
    public Result<Boolean> handleConnectException(ConnectException ex) {
        log.error("连接异常: {}", ex.getMessage(), ex);
        return Result.fail("服务器状态异常，请稍后再试！");
    }

    /**
     * 处理其他通用异常
     */
    @ExceptionHandler(value = {Exception.class})
    public Result<Boolean> handleOtherExceptions(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        return Result.fail("系统繁忙，请稍后再试！如问题持续存在，请联系技术支持");
    }
}
