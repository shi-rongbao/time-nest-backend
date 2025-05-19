package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 解锁状态枚举类
 */
@Getter
public enum UnlockedStatusEnum {

    UNLOCK(1, "已解锁"),
    LOCK(0, "未解锁");

    private final int code;
    private final String desc;

    UnlockedStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UnlockedStatusEnum getByCode(int code) {
        for (UnlockedStatusEnum value : UnlockedStatusEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> UnlockedStatusEnum code:{" + code + "}");
    }

}
