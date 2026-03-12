package com.pixiv.artwork.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 作品图片 DTO
 */
@Schema(description = "作品图片信息")
public class ImageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "图片原图URL（带水印）")
    private String imageUrl;

    @Schema(description = "原始图片URL（无水印，VIP可见）")
    private String originalImageUrl;

    @Schema(description = "图片缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    public ImageDTO() {
    }

    public ImageDTO(String imageUrl, String thumbnailUrl, Integer sortOrder) {
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.sortOrder = sortOrder;
    }

    public ImageDTO(String imageUrl, String originalImageUrl, String thumbnailUrl, Integer sortOrder) {
        this.imageUrl = imageUrl;
        this.originalImageUrl = originalImageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.sortOrder = sortOrder;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public void setOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
