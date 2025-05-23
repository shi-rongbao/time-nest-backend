package com.shirongbao.timenest.common.entity;


import lombok.Data;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * @author: ShiRongbao
 * @date: 2025-05-20
 * @description: 分页结果类
 */
@Data
public class PageResult<T> {

    @Setter
    private Integer pageNum = 1;

    @Setter
    private Integer pageSize = 10;

    private Integer total = 0;

    private Integer totalPages = 0;

    private List<T> records = Collections.emptyList();

    private Integer start = 0;

    private Integer end = 0;

    public void setRecords(List<T> records) {
        this.records = records;
        if (records != null && !records.isEmpty()) {
            setTotal(records.size());
        }
    }

    public void setTotal(Integer total) {
        this.total = total;
        if (this.pageSize > 0) {
            this.totalPages = (total / this.pageSize) + (total % this.pageSize == 0 ? 0 : 1);
        } else {
            this.totalPages = 0;
        }

        this.start = (this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0) + 1;
        this.end = (this.start - 1 + this.pageSize * (this.pageNum > 0 ? 1 : 0));
    }


}
