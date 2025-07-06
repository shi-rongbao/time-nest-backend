package com.shirongbao.timenest.strategy.wx;

import com.shirongbao.timenest.common.constant.RedisConstant;
import com.shirongbao.timenest.common.enums.MsgTypeEnum;
import com.shirongbao.timenest.service.auth.UserService;
import com.shirongbao.timenest.utils.RedisUtil;
import com.shirongbao.timenest.websocket.LoginWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: ShiRongbao
 * @date: 2025-07-05
 * @description:
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TextMsgStrategy implements MsgTypeStrategy {

    private final RedisUtil redisUtils;

    private final UserService userService;

    private final LoginWebSocketHandler loginWebSocketHandler;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.TEXT;
    }

    @Override
    public String getReturnContent(Map<String, String> requestBodyMap) {
        String content = requestBodyMap.get("Content");

        // 如果不是纯数字，提示去网站获取验证码
        if (!content.matches("\\d+")) {
            return "【拾光纪】请您登录官网：http://devsflow.cn 后获取验证码~";
        }

        Object value = redisUtils.get(RedisConstant.WX_LOGIN_VERIFY_CODE_PREFIX + content);
        if (value == null) {
            return "验证码已过期或不存在，请重新获取验证码！";
        }

        String sceneId = (String) value;
        if (StringUtils.isBlank(sceneId)) {
            return "验证码已过期或不存在，请重新获取验证码！";
        }

        // 验证码输入正确，就能去登录了，这个是用户的openId
        String fromUserName = requestBodyMap.get("FromUserName");
        String token = userService.wxLogin(fromUserName);
        loginWebSocketHandler.sendMessageToClient(sceneId, token);
        return "【拾光纪】恭喜您登录成功！";
    }

}
