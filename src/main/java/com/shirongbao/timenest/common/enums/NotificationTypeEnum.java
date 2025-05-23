package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 通知类型枚举类
 */
@Getter
public enum NotificationTypeEnum {

    FRIEND_REQUEST_NOTICE(1, "好友请求通知"),
    UNLOCK_NOTICE(0, "拾光纪解锁通知");

    private final int code;
    private final String desc;

    NotificationTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NotificationTypeEnum getByCode(int code) {
        for (NotificationTypeEnum value : NotificationTypeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> NotificationTypeEnum code:{" + code + "}");
    }

}
