package com.shirongbao.timecapsule.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 状态枚举类
 */
@Getter
public enum StatusEnum {

    NORMAL(1, "正常"),
    DISABLE(0, "禁用");

    private final int code;
    private final String desc;

    StatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StatusEnum getByCode(int code) {
        for (StatusEnum value : StatusEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> StatusEnum code:{" + code + "}");
    }

}
