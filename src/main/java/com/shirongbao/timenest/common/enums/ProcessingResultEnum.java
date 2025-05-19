package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-04-17
 * @description: 好友申请处理结果枚举类
 */
@Getter
public enum ProcessingResultEnum {

    ACCEPT(1, "接受"),
    REFUSE(0, "拒绝");

    private final int code;
    private final String desc;

    ProcessingResultEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProcessingResultEnum getByCode(int code) {
        for (ProcessingResultEnum value : ProcessingResultEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("不存在的枚举 -> ProcessingResultEnum code:{" + code + "}");
    }

}
