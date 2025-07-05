package com.shirongbao.timenest.controller.wx;

import com.shirongbao.timenest.utils.SHA1Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * @author: ShiRongbao
 * @date: 2025-07-04
 * @description:
 */
@RestController
@RequestMapping("/wx/")
@Slf4j
@RequiredArgsConstructor
public class CallBackController {

    // private final MsgTypeStrategyFactory msgTypeStrategyFactory;

    private static final String TOKEN = "shirongbao-token";

    @GetMapping(value = "test")
    public String test() {
        return "test";
    }

    @GetMapping("callback")
    public String callback(@RequestParam("signature") String signature,
                           @RequestParam("timestamp") String timestamp,
                           @RequestParam("nonce") String nonce,
                           @RequestParam("echostr") String echostr) {
        log.info("请求参数：signature:{}, timestamp:{}, nonce:{}, echostr{}", signature, timestamp, nonce, echostr);

        String sha1Str = SHA1Utils.getSHA1(TOKEN, timestamp, nonce, "");
        if (sha1Str == null) {
            return "error";
        }

        if (sha1Str.equals(signature)) {
            return echostr;
        }

        return "error";
    }

}
