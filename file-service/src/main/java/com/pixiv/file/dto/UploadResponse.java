package com.pixiv.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应 DTO
 * 
 * @author Pixiv Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    /**
     * 原图 URL（带水印）
     */
    private String imageUrl;

    /**
     * 缩略图 URL
     */
    private String thumbnailUrl;

    /**
     * 原始图片 URL（无水印，VIP可见）
     */
    private String originalImageUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String contentType;
}
