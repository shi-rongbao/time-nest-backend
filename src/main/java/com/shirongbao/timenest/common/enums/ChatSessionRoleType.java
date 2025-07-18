package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-07-18
 * @description: 聊天会话角色类型枚举类
 */
@Getter
public enum ChatSessionRoleType {
    MEMBER(1, "成员"),
    ADMIN(2, "管理员"),
    OWNER(3, "拥有者");

    private final int code;
    private final String desc;

    ChatSessionRoleType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ChatSessionRoleType getByCode(int code) {
        for (ChatSessionRoleType value : ChatSessionRoleType.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> ChatSessionRoleType code:{" + code + "}");
    }
}
