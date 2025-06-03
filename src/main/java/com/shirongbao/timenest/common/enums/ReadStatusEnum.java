package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-06-03
 * @description: 已读状态枚举
 */
@Getter
public enum ReadStatusEnum {

    READ_DONE(1, "已读"),
    UNREAD(0, "未读");

    private final int code;
    private final String desc;

    ReadStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReadStatusEnum getByCode(int code) {
        for (ReadStatusEnum value : ReadStatusEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> ReadStatusEnum code:{" + code + "}");
    }

}
