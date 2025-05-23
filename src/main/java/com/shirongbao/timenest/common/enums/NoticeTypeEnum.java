package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-05-23
 * @description: 通知类型枚举类
 */
@Getter
public enum NoticeTypeEnum {

    FRIEND_REQUEST_NOTICE(1, "好友请求通知"),
    TIME_NEST_NOTICE(0, "拾光纪解锁通知");

    private final int code;
    private final String desc;

    NoticeTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NoticeTypeEnum getByCode(int code) {
        for (NoticeTypeEnum value : NoticeTypeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> NoticeTypeEnum code:{" + code + "}");
    }

}
