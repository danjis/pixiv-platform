package com.pixiv.common.constant;

/**
 * 通用常量类
 * 定义系统中使用的通用常量
 */
public class CommonConstants {

    /**
     * UTF-8 编码
     */
    public static final String UTF8 = "UTF-8";

    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final int ERROR_CODE = 500;

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 默认页码（从 1 开始）
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * JWT 令牌请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * JWT 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户角色 - 普通用户
     */
    public static final String ROLE_USER = "USER";

    /**
     * 用户角色 - 画师
     */
    public static final String ROLE_ARTIST = "ARTIST";

    /**
     * 用户角色 - 管理员
     */
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * Redis 键前缀 - 用户信息
     */
    public static final String REDIS_KEY_USER = "user:";

    /**
     * Redis 键前缀 - 作品信息
     */
    public static final String REDIS_KEY_ARTWORK = "artwork:";

    /**
     * Redis 键前缀 - 刷新令牌
     */
    public static final String REDIS_KEY_REFRESH_TOKEN = "refresh_token:";

    /**
     * Redis 键前缀 - 验证码
     */
    public static final String REDIS_KEY_CAPTCHA = "captcha:";

    /**
     * 图片文件最大大小（10MB）
     */
    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L;

    /**
     * 支持的图片格式
     */
    public static final String[] SUPPORTED_IMAGE_FORMATS = {"jpg", "jpeg", "png", "gif"};

    /**
     * 缩略图宽度
     */
    public static final int THUMBNAIL_WIDTH = 300;

    /**
     * 缩略图高度
     */
    public static final int THUMBNAIL_HEIGHT = 300;

    /**
     * 日期时间格式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_PATTERN = "HH:mm:ss";
}
