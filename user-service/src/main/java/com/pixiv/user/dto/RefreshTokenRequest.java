package com.pixiv.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌请求 DTO
 */
public class RefreshTokenRequest {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    // Getters and Setters

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
