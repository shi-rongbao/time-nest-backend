package com.shirongbao.timecapsule.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 是否删除枚举
 */
@Getter
public enum IsDeletedEnum {

    DELETED(1, "已删除"),
    NOT_DELETED(0, "未删除");

    private final int code;
    private final String desc;

    IsDeletedEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static IsDeletedEnum getByCode(int code) {
        for (IsDeletedEnum value : IsDeletedEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> IsDeletedEnum code:{" + code + "}");
    }

}
