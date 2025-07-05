package com.shirongbao.timenest.pojo.vo;

import lombok.Data;

/**
 * @author: ShiRongbao
 * @date: 2025-07-05
 * @description: 验证码返回vo类
 */
@Data
public class VerifyCodeVo {

    // 场景id，唯一不重复
    private String sceneId;

    // 验证码 简单4位数字
    private int verifyCode;

}
