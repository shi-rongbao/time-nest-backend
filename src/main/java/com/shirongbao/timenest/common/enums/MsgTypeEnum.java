package com.shirongbao.timenest.common.enums;

import lombok.Getter;

/**
 * @author: ShiRongbao
 * @date: 2025-007-05
 * @description: 微信消息类型枚举类
 */
@Getter
public enum MsgTypeEnum {

    TEXT("text", "文本消息"),
    EVENT("event", "事件推送");

    private final String type;

    private final String desc;

    MsgTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static MsgTypeEnum getMsgTypeEnum(String type) {
        for (MsgTypeEnum msgTypeEnum : MsgTypeEnum.values()) {
            if (msgTypeEnum.getType().equals(type)) {
                return msgTypeEnum;
            }
        }
        throw new IllegalArgumentException("Not exist enum by type: {" + type + "}");
    }

}
