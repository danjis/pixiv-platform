package com.pixiv.user.dto;

import com.pixiv.user.entity.Artist;

import java.time.LocalDateTime;

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
}
