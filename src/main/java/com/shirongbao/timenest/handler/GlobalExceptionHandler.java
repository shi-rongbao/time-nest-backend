package com.shirongbao.timenest.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.shirongbao.timenest.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理sa-token 未登录异常
    @ExceptionHandler(value = {NotLoginException.class})
    public Result<Boolean> handleResourceNotFoundException(NotLoginException ex) {
        return Result.fail(ex.getMessage());
    }

    // 处理其他通用异常
    @ExceptionHandler(value = {Exception.class})
    public Result<Boolean> handleOtherExceptions(Exception ex) {
        return Result.fail(ex.getMessage());
    }
}
