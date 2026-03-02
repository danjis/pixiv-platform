package com.pixiv.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 画师申请请求 DTO
 */
public class ArtistApplicationRequest {
    
    @NotBlank(message = "作品集链接不能为空")
    @Size(max = 500, message = "作品集链接长度不能超过 500 个字符")
    private String portfolioUrl;
    
    @NotBlank(message = "个人简介不能为空")
    @Size(max = 2000, message = "个人简介长度不能超过 2000 个字符")
    private String description;
    
    // Constructors
    
    public ArtistApplicationRequest() {
    }
    
    public ArtistApplicationRequest(String portfolioUrl, String description) {
        this.portfolioUrl = portfolioUrl;
        this.description = description;
    }
    
    // Getters and Setters
    
    public String getPortfolioUrl() {
        return portfolioUrl;
    }
    
    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
