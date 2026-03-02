package com.pixiv.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应 DTO
 * 
 * 用于文件服务返回上传结果，包含原图和缩略图的 URL
 * 
 * @author Pixiv Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    /**
     * 原图 URL
     */
    private String imageUrl;

    /**
     * 缩略图 URL
     */
    private String thumbnailUrl;

    /**
     * 文件名（包含日期目录）
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件 MIME 类型
     */
    private String contentType;
}
