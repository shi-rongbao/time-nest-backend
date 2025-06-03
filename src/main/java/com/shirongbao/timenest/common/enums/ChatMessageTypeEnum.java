package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-06-03
 * @description: 聊天消息类型枚举
 */
@Getter
public enum ChatMessageTypeEnum {

    TEXT(1, "text"),
    IMAGE(2, "image"),
    FILE(3, "file");

    private final int code;
    private final String desc;

    ChatMessageTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ChatMessageTypeEnum getByCode(int code) {
        for (ChatMessageTypeEnum value : ChatMessageTypeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> ChatMessageTypeEnum code:{" + code + "}");
    }

}
