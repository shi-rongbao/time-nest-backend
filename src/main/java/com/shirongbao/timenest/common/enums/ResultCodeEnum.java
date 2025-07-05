package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: result枚举类
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    ;

    private final int code;

    private final String desc;

    ResultCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ResultCodeEnum getByCode(int code) {
        for (ResultCodeEnum value : ResultCodeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("不存在的结果状态码: " + code);
    }
}
