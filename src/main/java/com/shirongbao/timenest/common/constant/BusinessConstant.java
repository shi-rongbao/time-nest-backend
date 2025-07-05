package com.shirongbao.timenest.common.constant;

/**
 * @author: ShiRongbao
 * @date: 2025-06-27
 * @description: 业务常量类
 */
public final class BusinessConstant {

    private BusinessConstant() {
        // 防止实例化
    }

    // ========== 拾光纪相关常量 ==========
    /** 快要解锁的拾光纪最大查询数量 */
    public static final int MAX_UNLOCKING_NEST_COUNT = 6;

    /** 用户信息获取最大重试次数 */
    public static final int MAX_USER_INFO_RETRY_TIMES = 3;

    // ========== 分页相关常量 ==========
    /** 默认页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认页面大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大页面大小 */
    public static final int MAX_PAGE_SIZE = 100;

    // ========== 点赞相关常量 ==========
    /** 点赞标识 */
    public static final int LIKE_FLAG = 1;

    /** 未点赞标识 */
    public static final int NOT_LIKE_FLAG = 0;

    // ========== 文件上传相关常量 ==========
    /** 允许的图片文件扩展名 */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    /** 最大文件大小（字节）- 10MB */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

}
