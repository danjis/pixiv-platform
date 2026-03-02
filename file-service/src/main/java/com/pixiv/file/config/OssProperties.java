package com.pixiv.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性
 * 
 * 从 application.yml 中读取 aliyun.oss 配置
 * 
 * @author Pixiv Platform Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /**
     * OSS 访问端点（如：oss-cn-hangzhou.aliyuncs.com）
     */
    private String endpoint;

    /**
     * 访问密钥 ID
     */
    private String accessKeyId;

    /**
     * 访问密钥 Secret
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 缩略图配置
     */
    private Thumbnail thumbnail = new Thumbnail();

    /**
     * 缩略图配置类
     */
    @Data
    public static class Thumbnail {
        /**
         * 缩略图宽度（默认 300px）
         */
        private int width = 300;

        /**
         * 缩略图高度（默认 300px）
         */
        private int height = 300;

        /**
         * 缩略图质量（0.0-1.0，默认 0.8）
         */
        private double quality = 0.8;
    }
}
