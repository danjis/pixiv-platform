package com.pixiv.file.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 文件服务常量
 * 
 * @author Pixiv Platform Team
 */
public class FileConstants {

    /**
     * 允许的图片文件类型
     */
    public static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif");

    /**
     * 允许的文件扩展名
     */
    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg",
            "jpeg",
            "png",
            "gif");

    /**
     * 最大文件大小（10MB）
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 最大头像文件大小（5MB）
     */
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    /**
     * OSS 文件路径前缀（作品图片）
     */
    public static final String OSS_PATH_PREFIX = "artworks/";

    /**
     * 缩略图路径前缀
     */
    public static final String THUMBNAIL_PATH_PREFIX = "thumbnails/";

    /**
     * 头像路径前缀
     */
    public static final String AVATAR_PATH_PREFIX = "avatars/";

    private FileConstants() {
        // 工具类，禁止实例化
    }
}
