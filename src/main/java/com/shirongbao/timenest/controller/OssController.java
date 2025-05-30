package com.shirongbao.timenest.controller;

import com.shirongbao.timenest.anno.RateLimit;
import com.shirongbao.timenest.service.oss.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author: ShiRongbao
 * @date: 2025-05-16
 * @description: oss接口控制器
 */
@RequestMapping("/oss")
@RestController
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @RateLimit(minuteLimit = 3, hourLimit = 20)
    @PostMapping("/upload")
    public String test(@RequestParam("file") MultipartFile file) throws IOException {
        return ossService.uploadAvatar(file);
    }

}
