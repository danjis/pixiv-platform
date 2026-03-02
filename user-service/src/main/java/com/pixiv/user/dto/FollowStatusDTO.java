package com.pixiv.user.dto;

/**
 * 关注状态 DTO
 */
public class FollowStatusDTO {

    private boolean isFollowing;

    public FollowStatusDTO() {
    }

    public FollowStatusDTO(boolean isFollowing) {
        this.isFollowing = isFollowing;
    }

    // Getters and Setters

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }
}
