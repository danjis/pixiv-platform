package com.pixiv.user.dto;

import com.pixiv.user.entity.ApplicationStatus;
import com.pixiv.user.entity.ArtistApplication;

import java.time.LocalDateTime;

/**
 * 画师申请 DTO
 */
public class ArtistApplicationDTO {

    private Long id;
    private Long userId;
    private String username;
    private String portfolioUrl;
    private String description;
    private ApplicationStatus status;
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    // Constructors

    public ArtistApplicationDTO() {
    }

    public ArtistApplicationDTO(ArtistApplication application) {
        this.id = application.getId();
        this.userId = application.getUserId();
        this.portfolioUrl = application.getPortfolioUrl();
        this.description = application.getDescription();
        this.status = application.getStatus();
        this.reviewerId = application.getReviewerId();
        this.reviewComment = application.getReviewComment();
        this.createdAt = application.getCreatedAt();
        this.reviewedAt = application.getReviewedAt();
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

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
