package com.shirongbao.timenest.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: 拾光纪Dto类
 */
@Data
public class TimeNestDto {

    @NotNull(message = "nestId不能为空")
    private Long id;

}
