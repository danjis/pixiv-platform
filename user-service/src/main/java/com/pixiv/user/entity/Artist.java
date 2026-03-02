package com.pixiv.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 画师实体类
 * 与 User 实体一对一关系，存储画师的扩展信息
 */
@Entity
@Table(name = "artists")
public class Artist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "follower_count", nullable = false)
    private Integer followerCount = 0;
    
    @Column(name = "artwork_count", nullable = false)
    private Integer artworkCount = 0;
    
    @Column(name = "commission_count", nullable = false)
    private Integer commissionCount = 0;
    
    @Column(name = "accepting_commissions", nullable = false)
    private Boolean acceptingCommissions = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
