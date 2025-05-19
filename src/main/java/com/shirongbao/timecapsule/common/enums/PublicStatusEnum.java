package com.shirongbao.timecapsule.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: capsule类型枚举
 */
@Getter
public enum PublicStatusEnum {

    PUBLIC(1, "公开"),
    PRIVATE(0, "私密");

    private final int code;
    private final String desc;

    PublicStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PublicStatusEnum getByCode(int code) {
        for (PublicStatusEnum value : PublicStatusEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> PublicStatusEnum code:{" + code + "}");
    }

}
