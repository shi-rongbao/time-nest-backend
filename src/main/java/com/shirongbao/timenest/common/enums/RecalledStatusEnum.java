package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-07-19
 * @description: 撤回状态枚举类
 */
@Getter
public enum RecalledStatusEnum {

    NORMAL(0, "正常"),
    RECALLED(1, "撤回");

    private final int code;
    private final String desc;

    RecalledStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static RecalledStatusEnum getByCode(int code) {
        for (RecalledStatusEnum value : RecalledStatusEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> RecalledStatusEnum code:{" + code + "}");
    }

}
