package com.shirongbao.timenest.controller;

import ai.z.openapi.ZhipuAiClient;
import com.shirongbao.timenest.common.entity.Result;
import com.shirongbao.timenest.pojo.dto.InputDto;
import com.shirongbao.timenest.service.ai.ZhipuService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: ShiRongbao
 * @date: 2025-09-30
 * @description: 调用大模型
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final ZhipuService zhipuService;

    // 总结文本输入
    @PostMapping("/summarize")
    public Result<String> summarize(@RequestBody InputDto inputDto) {
        String input = inputDto.getInput();
        if (StringUtils.isBlank(input)) {
            return Result.fail("输入不能为空");
        }

        String output = zhipuService.summaryInput(input);
        return Result.success(output);
    }

}
