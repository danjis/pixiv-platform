package com.pixiv.user.dto;

import com.pixiv.user.entity.UserMembership;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MembershipDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long userId;
    private String level;
    private String expireTime;
    private boolean expired;
    private String username;

    public MembershipDTO() {
    }

    public MembershipDTO(UserMembership membership) {
        this.userId = membership.getUserId();
        this.level = membership.getLevel().name();
        if (membership.getExpireTime() != null) {
            this.expireTime = membership.getExpireTime().format(FORMATTER);
            this.expired = membership.getExpireTime().isBefore(LocalDateTime.now());
        } else {
            this.expireTime = null;
            this.expired = false;
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
