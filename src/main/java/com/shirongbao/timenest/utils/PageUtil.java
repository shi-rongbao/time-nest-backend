package com.shirongbao.timenest.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.function.Function;

/**
 * @author: ShiRongbao
 * @date: 2025-06-27
 * @description: 分页工具类
 */
public final class PageUtil {

    private PageUtil() {
        // 防止实例化
    }

    /**
     * 转换分页对象
     *
     * @param sourcePage 源分页对象
     * @param converter  转换函数
     * @param <S>        源类型
     * @param <T>        目标类型
     * @return 转换后的分页对象
     */
    public static <S, T> Page<T> convertPage(Page<S> sourcePage, Function<List<S>, List<T>> converter) {
        if (sourcePage == null) {
            return new Page<>();
        }

        List<S> sourceRecords = sourcePage.getRecords();
        List<T> targetRecords = converter.apply(sourceRecords);

        Page<T> targetPage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        targetPage.setRecords(targetRecords);
        return targetPage;
    }

    /**
     * 转换分页对象（单个对象转换）
     *
     * @param sourcePage 源分页对象
     * @param converter  单个对象转换函数
     * @param <S>        源类型
     * @param <T>        目标类型
     * @return 转换后的分页对象
     */
    public static <S, T> Page<T> convertPageSingle(Page<S> sourcePage, Function<S, T> converter) {
        return convertPage(sourcePage, sourceList -> 
            sourceList.stream().map(converter).toList()
        );
    }

}
