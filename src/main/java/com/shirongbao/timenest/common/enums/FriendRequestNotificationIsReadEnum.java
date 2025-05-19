package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 注销申请状态枚举
 */
@Getter
public enum FriendRequestNotificationIsReadEnum {

    NOT_READ(0, "未读"),
    READ(1, "已读");

    private final int code;
    private final String desc;

    FriendRequestNotificationIsReadEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FriendRequestNotificationIsReadEnum getByCode(int code) {
        for (FriendRequestNotificationIsReadEnum value : FriendRequestNotificationIsReadEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> FriendRequestNotificationIsReadEnum code:{" + code + "}");
    }

}
