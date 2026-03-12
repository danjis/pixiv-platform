package com.pixiv.artwork.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 创建作品请求 DTO
 * 
 * 用于画师发布新作品时提交的数据
 */
@Schema(description = "创建作品请求")
public class CreateArtworkRequest {

    /**
     * 作品标题
     */
    @Schema(description = "作品标题", example = "初音未来在樱花树下")
    @NotBlank(message = "作品标题不能为空")
    @Size(max = 200, message = "作品标题长度不能超过 200 个字符")
    private String title;

    /**
     * 作品描述
     */
    @Schema(description = "作品描述", example = "这是一幅描绘初音未来在春日樱花树下的作品。画面中，初音未来身穿标志性的绿色服装，双马尾随风飘扬，背景是盛开的粉色樱花。")
    @Size(max = 5000, message = "作品描述长度不能超过 5000 个字符")
    private String description;

    /**
     * 原图 URL（从文件服务上传后获得）
     */
    @Schema(description = "原图 URL（单图模式，与 images 二选一）", example = "https://example.oss-cn-hangzhou.aliyuncs.com/artworks/miku-sakura-001.jpg")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$", message = "图片 URL 格式不正确，必须是 http:// 或 https:// 开头，且以 .jpg/.jpeg/.png/.gif/.webp 结尾", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String imageUrl;

    /**
     * 缩略图 URL（从文件服务上传后获得）
     */
    @Schema(description = "缩略图 URL（从文件服务上传后获得）", example = "https://example.oss-cn-hangzhou.aliyuncs.com/artworks/thumb_miku-sakura-001.jpg")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$", message = "缩略图 URL 格式不正确，必须是 http:// 或 https:// 开头，且以 .jpg/.jpeg/.png/.gif/.webp 结尾", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String thumbnailUrl;

    /**
     * 手动添加的标签列表（可选）
     */
    @Schema(description = "手动添加的标签列表（可选，系统会自动进行 AI 智能打标）", example = "[\"初音未来\", \"樱花\", \"春天\", \"VOCALOID\"]")
    private List<String> tags;

    /**
     * 是否为 AIGC（AI 生成）作品
     */
    @Schema(description = "多图列表（与 imageUrl 二选一，优先使用此字段）")
    private List<ImageItem> images;

    @Schema(description = "是否为 AI 生成内容（AIGC）作品", example = "false")
    private Boolean isAigc;

    @Schema(description = "是否保存为草稿（true=草稿, false/null=直接发布）", example = "false")
    private Boolean isDraft;

    // Getters and Setters

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public Boolean getIsAigc() {
        return isAigc;
    }

    public void setIsAigc(Boolean isAigc) {
        this.isAigc = isAigc;
    }

    public Boolean getIsDraft() {
        return isDraft;
    }

    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }

    /**
     * 多图上传时的单张图片信息
     */
    @Schema(description = "单张图片信息")
    public static class ImageItem {
        @Schema(description = "原图 URL（带水印）")
        private String imageUrl;
        @Schema(description = "缩略图 URL")
        private String thumbnailUrl;
        @Schema(description = "原始图片URL（无水印，VIP可见）")
        private String originalImageUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getOriginalImageUrl() {
            return originalImageUrl;
        }

        public void setOriginalImageUrl(String originalImageUrl) {
            this.originalImageUrl = originalImageUrl;
        }
    }
}
