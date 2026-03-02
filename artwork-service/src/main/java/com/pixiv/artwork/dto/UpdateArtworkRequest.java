package com.pixiv.artwork.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 更新作品请求 DTO
 * 
 * 用于画师编辑已发布作品或草稿的标题、描述、标签和图片
 */
@Schema(description = "更新作品请求")
public class UpdateArtworkRequest {

    @Schema(description = "作品标题", example = "初音未来在樱花树下（修改版）")
    @Size(max = 200, message = "作品标题长度不能超过 200 个字符")
    private String title;

    @Schema(description = "作品描述", example = "更新后的作品描述")
    @Size(max = 5000, message = "作品描述长度不能超过 5000 个字符")
    private String description;

    @Schema(description = "手动标签列表（会替换原有的手动标签，AI标签保持不变）", example = "[\"初音未来\", \"樱花\", \"春天\"]")
    private List<String> tags;

    @Schema(description = "多图列表（更新作品图片）")
    private List<CreateArtworkRequest.ImageItem> images;

    @Schema(description = "是否保存为草稿（true=草稿, false/null=发布）", example = "false")
    private Boolean isDraft;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<CreateArtworkRequest.ImageItem> getImages() {
        return images;
    }

    public void setImages(List<CreateArtworkRequest.ImageItem> images) {
        this.images = images;
    }

    public Boolean getIsDraft() {
        return isDraft;
    }

    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }
}
