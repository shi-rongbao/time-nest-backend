package com.shirongbao.timecapsule.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description:
 */
@Getter
public enum DeactivationRequestedEnum {

    YES(1, "申请注销中"),
    NO(0, "未申请注销");

    private final int code;
    private final String desc;

    DeactivationRequestedEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DeactivationRequestedEnum getByCode(int code) {
        for (DeactivationRequestedEnum value : DeactivationRequestedEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> DeactivationRequestedEnum code:{" + code + "}");
    }

}
