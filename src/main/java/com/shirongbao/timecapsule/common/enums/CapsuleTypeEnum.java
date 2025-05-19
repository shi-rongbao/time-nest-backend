package com.shirongbao.timecapsule.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: capsule类型枚举
 */
@Getter
public enum CapsuleTypeEnum {

    CAPSULE(1, "胶囊"),
    MAIL(2, "邮箱"),
    IMAGE(3, "图片");

    private final int code;
    private final String desc;

    CapsuleTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CapsuleTypeEnum getByCode(int code) {
        for (CapsuleTypeEnum value : CapsuleTypeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> CapsuleTypeEnum code:{" + code + "}");
    }

}
