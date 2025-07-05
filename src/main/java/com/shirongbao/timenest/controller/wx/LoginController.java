package com.shirongbao.timenest.controller.wx;

import com.shirongbao.timenest.anno.RateLimit;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.pojo.vo.VerifyCodeVo;
import com.shirongbao.timenest.service.auth.UserService;
import com.shirongbao.timenest.strategy.wx.MsgTypeStrategy;
import com.shirongbao.timenest.strategy.wx.MsgTypeStrategyFactory;
import com.shirongbao.timenest.utils.SHA1Utils;
import com.shirongbao.timenest.utils.XmlMapParser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    private final MsgTypeStrategyFactory msgTypeStrategyFactory;

    private static final String TOKEN = "shiguangji";

    @GetMapping("/callback")
    public Long callback(@RequestParam("signature") String signature,
                           @RequestParam("timestamp") String timestamp,
                           @RequestParam("nonce") String nonce,
                           @RequestParam("echostr") String echostr) {
        String sha1Str = SHA1Utils.getSHA1(TOKEN, timestamp, nonce, "");
        if (sha1Str == null) {
            return -1L;
        }

        if (sha1Str.equals(signature)) {
            return Long.valueOf(echostr);
        }

        return -1L;
    }

    // 获取验证码(微信登录，简单4位数字验证码，也要缓存5分钟)
    @RateLimit(minuteLimit = 3, hourLimit = 20)
    @GetMapping("/getVerifyCode")
    public Result<VerifyCodeVo> getVerifyCode(){
        return userService.getVerifyCode();
    }

    // 接收微信消息（微信服务器调用方法）
    @PostMapping(value = "/callback", produces = "application/xml;charset=UTF-8")
    public String callback(
            @RequestBody String requestBody,
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam(value = "msg_signature", required = false) String msgSignature) throws Exception {
        String sha1Str = SHA1Utils.getSHA1(TOKEN, timestamp, nonce, "");

        // 验签不通过返回！
        if (sha1Str == null || !sha1Str.equals(signature)) {
            return "error";
        }

        Map<String, String> requestBodyMap = XmlMapParser.parseXmlToMap(requestBody);
        String msgType = requestBodyMap.get("MsgType");
        MsgTypeStrategy strategy = msgTypeStrategyFactory.getStrategy(msgType);
        if (strategy == null) {
            return "";
        }
        String respContent = strategy.getReturnContent(requestBodyMap);

        String fromUserName = requestBodyMap.get("FromUserName");
        String toUserName = requestBodyMap.get("ToUserName");
        return "<xml>\n" +
                "  <ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>\n" +
                "  <FromUserName><![CDATA[" + toUserName + "]]></FromUserName>\n" +
                "  <CreateTime>12345678</CreateTime>\n" +
                "  <MsgType><![CDATA[text]]></MsgType>\n" +
                "  <Content><![CDATA[" + respContent + "]]></Content>\n" +
                "</xml>";
    }

}
