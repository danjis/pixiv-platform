package com.pixiv.artwork.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 提交参赛作品请求 DTO
 */
public class SubmitEntryRequest {

    @NotBlank(message = "作品标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "作品图片不能为空")
    private String imageUrl;

    private String thumbnailUrl;

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
