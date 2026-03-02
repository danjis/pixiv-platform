package com.pixiv.artwork.dto;

import com.pixiv.artwork.entity.TagSource;

/**
 * 标签 DTO
 * 
 * 用于返回标签信息
 */
public class TagDTO {
    
    private Long id;
    private String name;
    private TagSource source;
    private Float confidence;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public TagSource getSource() {
        return source;
    }
    
    public void setSource(TagSource source) {
        this.source = source;
    }
    
    public Float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }
}
