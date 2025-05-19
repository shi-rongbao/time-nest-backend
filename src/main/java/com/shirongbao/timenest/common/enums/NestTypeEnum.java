package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: nest类型枚举
 */
@Getter
public enum NestTypeEnum {

    CAPSULE(1, "胶囊"),
    MAIL(2, "邮箱"),
    IMAGE(3, "图片");

    private final int code;
    private final String desc;

    NestTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NestTypeEnum getByCode(int code) {
        for (NestTypeEnum value : NestTypeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> NestTypeEnum code:{" + code + "}");
    }

}
