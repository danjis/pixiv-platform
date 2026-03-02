package com.pixiv.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 审核画师申请请求 DTO
 */
public class ReviewApplicationRequest {
    
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
    
    @Size(max = 500, message = "审核意见长度不能超过 500 个字符")
    private String reviewComment;
    
    // Constructors
    
    public ReviewApplicationRequest() {
    }
    
    public ReviewApplicationRequest(Boolean approved, String reviewComment) {
        this.approved = approved;
        this.reviewComment = reviewComment;
    }
    
    // Getters and Setters
    
    public Boolean getApproved() {
        return approved;
    }
    
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    
    public String getReviewComment() {
        return reviewComment;
    }
    
    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
