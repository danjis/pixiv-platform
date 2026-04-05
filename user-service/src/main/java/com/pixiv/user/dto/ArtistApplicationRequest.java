package com.pixiv.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

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

    @Size(max = 10, message = "擅长领域最多 10 项")
    private List<@Size(max = 30, message = "擅长领域单项长度不能超过 30 个字符") String> specialties;

    @Size(max = 200, message = "联系方式长度不能超过 200 个字符")
    private String contactInfo;

    // Constructors

    public ArtistApplicationRequest() {
    }

    public ArtistApplicationRequest(String portfolioUrl, String description, List<String> specialties,
            String contactInfo) {
        this.portfolioUrl = portfolioUrl;
        this.description = description;
        this.specialties = specialties;
        this.contactInfo = contactInfo;
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

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
