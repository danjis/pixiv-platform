package com.pixiv.user.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新个人信息请求 DTO
 */
public class UpdateProfileRequest {

    @Size(max = 500, message = "头像 URL 长度不能超过 500 个字符")
    private String avatarUrl;

    @Size(max = 1000, message = "个人简介长度不能超过 1000 个字符")
    private String bio;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String avatarUrl, String bio) {
        this.avatarUrl = avatarUrl;
        this.bio = bio;
    }

    // Getters and Setters

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
