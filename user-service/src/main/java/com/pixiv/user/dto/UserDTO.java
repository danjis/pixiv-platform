package com.pixiv.user.dto;

import com.pixiv.user.entity.User;

import java.time.LocalDateTime;

/**
 * 用户信息 DTO
 */
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private String role; // 改为 String 类型，与 common 模块的 UserDTO 保持一致
    private LocalDateTime createdAt;

    // 统计数据
    private Long artworkCount; // 作品数（仅画师）
    private Long followerCount; // 粉丝数（仅画师）
    private Long followingCount; // 关注数

    // 隐私设置
    private Boolean hideFollowing; // 是否隐藏关注列表
    private Boolean hideFavorites; // 是否隐藏收藏列表

    // 会员等级
    private String membershipLevel; // NORMAL / VIP / SVIP

    public UserDTO() {
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.avatarUrl = user.getAvatarUrl();
        this.bio = user.getBio();
        this.role = user.getRole().name(); // 转换为字符串
        this.createdAt = user.getCreatedAt();
        this.hideFollowing = user.getHideFollowing();
        this.hideFavorites = user.getHideFavorites();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getArtworkCount() {
        return artworkCount;
    }

    public void setArtworkCount(Long artworkCount) {
        this.artworkCount = artworkCount;
    }

    public Long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Long followerCount) {
        this.followerCount = followerCount;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    public Boolean getHideFollowing() {
        return hideFollowing;
    }

    public void setHideFollowing(Boolean hideFollowing) {
        this.hideFollowing = hideFollowing;
    }

    public Boolean getHideFavorites() {
        return hideFavorites;
    }

    public void setHideFavorites(Boolean hideFavorites) {
        this.hideFavorites = hideFavorites;
    }

    public String getMembershipLevel() {
        return membershipLevel;
    }

    public void setMembershipLevel(String membershipLevel) {
        this.membershipLevel = membershipLevel;
    }
}
