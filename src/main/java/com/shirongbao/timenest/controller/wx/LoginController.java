package com.shirongbao.timenest.controller.wx;

import com.shirongbao.timenest.anno.RateLimit;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.pojo.vo.VerifyCodeVo;
import com.shirongbao.timenest.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author: ShiRongbao
 * @date: 2025-07-05
 * @description: 微信登录接口控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/wx/login")
public class LoginController {

    private final UserService userService;

    // 获取验证码(微信登录，简单4位数字验证码，也要缓存5分钟)
    @RateLimit(minuteLimit = 1, hourLimit = 20)
    @GetMapping("/getVerifyCode")
    public Result<VerifyCodeVo> getVerifyCode(){
        return userService.getVerifyCode();
    }

}
