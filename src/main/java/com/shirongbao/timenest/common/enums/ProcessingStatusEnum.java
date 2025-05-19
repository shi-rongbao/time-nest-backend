package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 处理状态枚举
 */
@Getter
public enum ProcessingStatusEnum {

    WAITING(0, "等待处理"),
    PASSING(1, "处理通过");

    private final int code;
    private final String desc;

    ProcessingStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProcessingStatusEnum getByCode(int code) {
        for (ProcessingStatusEnum value : ProcessingStatusEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> ProcessingStatusEnum code:{" + code + "}");
    }

}
