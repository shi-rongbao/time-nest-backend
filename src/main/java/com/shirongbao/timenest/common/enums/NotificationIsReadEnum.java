package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 通知是否读枚举类
 */
@Getter
public enum NotificationIsReadEnum {

    NOT_READ(0, "未读"),
    READ(1, "已读");

    private final int code;
    private final String desc;

    NotificationIsReadEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NotificationIsReadEnum getByCode(int code) {
        for (NotificationIsReadEnum value : NotificationIsReadEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> NotificationIsReadEnum code:{" + code + "}");
    }

}
