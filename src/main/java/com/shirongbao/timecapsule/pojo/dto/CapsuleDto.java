package com.shirongbao.timecapsule.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author: ShiRongbao
 * @date: 2025-05-18
 * @description: capsule Dto
 */
@Data
public class CapsuleDto {

    @NotNull(message = "capsuleId不能为空")
    private Long id;

}
