package com.shirongbao.timenest.common.entity;

/**
 * @author: ShiRongbao
 * @date: 2025-05-20
 * @description: 分页请求参数
 */
public class PageInfo {

    private Integer pageNum;

    private Integer pageSize;

    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return pageSize;
    }
}
