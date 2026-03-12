package com.pixiv.user.dto;

import com.pixiv.user.entity.Artist;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 画师信息 DTO
 */
public class ArtistDTO {

    private Long id;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String portfolioUrl;
    private String description;
    private List<String> specialties;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String contactPreference;
    private String contactInfo;
    private Integer followerCount;
    private Integer artworkCount;
    private Integer commissionCount;
    private Boolean acceptingCommissions;
    private LocalDateTime createdAt;

    public ArtistDTO() {
    }

    public ArtistDTO(Artist artist) {
        this.id = artist.getId();
        this.userId = artist.getUser().getId();
        this.username = artist.getUser().getUsername();
        this.avatarUrl = artist.getUser().getAvatarUrl();
        this.portfolioUrl = artist.getPortfolioUrl();
        this.description = artist.getDescription();
        this.followerCount = artist.getFollowerCount();
        this.artworkCount = artist.getArtworkCount();
        this.commissionCount = artist.getCommissionCount();
        this.acceptingCommissions = artist.getAcceptingCommissions();
        this.createdAt = artist.getCreatedAt();
        this.minPrice = artist.getMinPrice();
        this.maxPrice = artist.getMaxPrice();
        this.contactPreference = artist.getContactPreference();
        this.contactInfo = artist.getContactInfo();
        // 解析 JSON 字符串为 List
        if (artist.getSpecialties() != null && !artist.getSpecialties().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                this.specialties = mapper.readValue(artist.getSpecialties(),
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (Exception e) {
                this.specialties = List.of();
            }
        } else {
            this.specialties = List.of();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

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

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public Integer getArtworkCount() {
        return artworkCount;
    }

    public void setArtworkCount(Integer artworkCount) {
        this.artworkCount = artworkCount;
    }

    public Integer getCommissionCount() {
        return commissionCount;
    }

    public void setCommissionCount(Integer commissionCount) {
        this.commissionCount = commissionCount;
    }

    public Boolean getAcceptingCommissions() {
        return acceptingCommissions;
    }

    public void setAcceptingCommissions(Boolean acceptingCommissions) {
        this.acceptingCommissions = acceptingCommissions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getContactPreference() {
        return contactPreference;
    }

    public void setContactPreference(String contactPreference) {
        this.contactPreference = contactPreference;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
