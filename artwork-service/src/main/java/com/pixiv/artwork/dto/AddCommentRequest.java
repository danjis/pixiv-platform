package com.pixiv.artwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 添加评价请求
 */
public class AddCommentRequest {

    /**
     * 评价内容
     */
    @NotBlank(message = "评价内容不能为空")
    @Size(max = 1000, message = "评价内容不能超过 1000 个字符")
    private String content;

    /**
     * 父评论 ID（可选，NULL 表示顶级评论）
     */
    private Long parentId;

    // Constructors

    public AddCommentRequest() {
    }

    public AddCommentRequest(String content) {
        this.content = content;
    }

    public AddCommentRequest(String content, Long parentId) {
        this.content = content;
        this.parentId = parentId;
    }

    // Getters and Setters

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
