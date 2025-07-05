package com.shirongbao.timenest.common.enums;

import com.shirongbao.timenest.common.exception.BusinessException;
import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 通知读取状态枚举类
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
        throw new BusinessException("不存在的通知读取状态枚举，code: " + code);
    }

}
