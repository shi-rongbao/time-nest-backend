package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 点赞类型枚举类
 */
@Getter
public enum LikeTypeEnum {

    LIKE(1, "点赞"),
    NOT_LIKE(0, "取消点赞");

    private final int code;
    private final String desc;

    LikeTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LikeTypeEnum getByCode(int code) {
        for (LikeTypeEnum value : LikeTypeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> LikeTypeEnum code:{" + code + "}");
    }

}
