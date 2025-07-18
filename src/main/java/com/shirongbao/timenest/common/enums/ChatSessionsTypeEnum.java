package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-07-18
 * @description: 聊天会话类型枚举类
 */
@Getter
public enum ChatSessionsTypeEnum {

    SINGLE(1, "单聊"),
    GROUP(2, "群聊");

    private final int code;
    private final String desc;

    ChatSessionsTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ChatSessionsTypeEnum getByCode(int code) {
        for (ChatSessionsTypeEnum value : ChatSessionsTypeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> ChatSessionsTypeEnum code:{" + code + "}");
    }

}
