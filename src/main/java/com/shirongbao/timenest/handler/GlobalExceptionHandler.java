package com.shirongbao.timenest.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.shirongbao.timenest.common.entity.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理 sa-token 未登录异常
    @ExceptionHandler(value = {NotLoginException.class})
    public Result<Boolean> handleNotLoginException(NotLoginException ex) {
        return Result.fail("当前账号登录状态异常，请重新登录！");
    }

    // 处理连接异常
    @ExceptionHandler(value = {ConnectException.class})
    public Result<Boolean> handleConnectException(NotLoginException ex) {
        return Result.fail("服务器状态异常，请稍后再试！");
    }

    // 处理其他通用异常
    @ExceptionHandler(value = {Exception.class})
    public Result<Boolean> handleOtherExceptions(Exception ex) {
        return Result.fail("出问题了！(┬＿┬)");
    }
}
