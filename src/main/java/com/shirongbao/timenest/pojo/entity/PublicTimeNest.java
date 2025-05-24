package com.shirongbao.timenest.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author: ShiRongbao
 * @date: 2025-05-24
 * @description: 公开拾光纪表实体
 */
@Data
public class PublicTimeNest {

    // 主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // nest的id
    private Long timeNestId;

    // 公开时间
    private Date publicTime;

    // 创建时间
    private Date createdAt;

}
