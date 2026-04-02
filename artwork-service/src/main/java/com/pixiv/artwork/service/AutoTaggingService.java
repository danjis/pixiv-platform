package com.pixiv.artwork.service;

import com.pixiv.artwork.entity.Tag;
import com.pixiv.artwork.entity.TagSource;
import com.pixiv.artwork.entity.ArtworkTag;
import com.pixiv.artwork.repository.TagRepository;
import com.pixiv.artwork.repository.ArtworkTagRepository;
import com.pixiv.artwork.repository.ArtworkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.ArrayList;

/**
 * 自动打标服务
 * 
 * 负责调用 Python AI 服务进行智能打标，并将标签保存到数据库
 */
@Service
public class AutoTaggingService {

    private static final Logger logger = LoggerFactory.getLogger(AutoTaggingService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ArtworkTagRepository artworkTagRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ARTWORK_CACHE_PREFIX = "artwork:";

    /**
     * AI 服务的基础 URL
     * 从配置文件中读取，默认为 http://localhost:8000
     */
    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    /**
     * 处理智能打标请求
     * 
     * 实现降级策略：AI 服务不可用时不阻塞作品发布，记录错误日志
     * 
     * @param artworkId 作品 ID
     * @param imageUrl  图片 URL
     */
    @Transactional
    public void processTagging(Long artworkId, String imageUrl) {
        logger.info("开始处理智能打标: artworkId={}, imageUrl={}", artworkId, imageUrl);

        try {
            // 1. 验证作品是否存在
            if (!artworkRepository.existsById(artworkId)) {
                logger.warn("作品不存在，跳过打标: artworkId={}", artworkId);
                return;
            }

            // 2. 调用 AI 服务获取标签（带降级处理）
            TaggingResponse response = callAiServiceWithFallback(imageUrl);

            if (response == null || response.getTags() == null || response.getTags().isEmpty()) {
                logger.warn("AI 服务未返回标签: artworkId={}", artworkId);
                return;
            }

            logger.info("AI 服务返回 {} 个标签: artworkId={}", response.getTags().size(), artworkId);

            // 3. 保存标签到数据库
            saveTags(artworkId, response.getTags());

            logger.info("智能打标完成: artworkId={}, 保存了 {} 个标签",
                    artworkId, response.getTags().size());

        } catch (Exception e) {
            // 降级策略：失败不抛出异常，只记录日志
            // 这样不会阻塞作品发布流程，用户可以手动添加标签
            logger.error("智能打标处理失败（已降级）: artworkId={}, error={}",
                    artworkId, e.getMessage(), e);
        }
    }

    /**
     * 调用 AI 服务（带降级处理）
     * 
     * @param imageUrl 图片 URL
     * @return 标签响应，失败时返回 null
     */
    private TaggingResponse callAiServiceWithFallback(String imageUrl) {
        try {
            return callAiService(imageUrl);
        } catch (RestClientException e) {
            // AI 服务不可用时的降级处理
            logger.error("AI 服务不可用（已降级）: error={}", e.getMessage());
            return null;
        } catch (Exception e) {
            // 其他异常的降级处理
            logger.error("调用 AI 服务时发生未知错误（已降级）: error={}", e.getMessage());
            return null;
        }
    }

    /**
     * 调用 AI 服务获取标签
     * 
     * @param imageUrl 图片 URL
     * @return 标签响应
     */
    private TaggingResponse callAiService(String imageUrl) {
        String url = aiServiceUrl + "/api/predict";

        TaggingRequest request = new TaggingRequest();
        request.setImageUrl(imageUrl);

        logger.debug("调用 AI 服务: url={}, imageUrl={}", url, imageUrl);

        try {
            TaggingResponse response = restTemplate.postForObject(
                    url, request, TaggingResponse.class);

            logger.debug("AI 服务响应成功: tags={}",
                    response != null ? response.getTags().size() : 0);

            return response;

        } catch (RestClientException e) {
            logger.error("AI 服务调用失败: url={}, error={}", url, e.getMessage());
            throw e;
        }
    }

    /**
     * 保存标签到数据库
     * 
     * @param artworkId 作品 ID
     * @param tagInfos  标签信息列表
     */
    @Transactional
    public void saveTags(Long artworkId, List<TagInfo> tagInfos) {
        logger.info("开始保存自动标签: artworkId={}, 标签数={}", artworkId, tagInfos.size());

        int savedCount = 0;

        for (TagInfo tagInfo : tagInfos) {
            try {
                // 1. 使用英文标签名作为唯一标识
                String englishName = tagInfo.getTag();
                String chineseName = tagInfo.getTagZh();

                // 2. 查找或创建标签（以英文名为主键）
                Tag tag = tagRepository.findByName(englishName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(englishName);
                            newTag.setUsageCount(0);
                            return newTag;
                        });

                // 更新中文翻译（如果有新翻译）
                if (chineseName != null && !chineseName.isEmpty() && !chineseName.equals(englishName)) {
                    tag.setNameZh(chineseName);
                }
                tagRepository.save(tag);

                // 2. 检查是否已存在关联
                if (artworkTagRepository.existsByArtworkIdAndTagId(artworkId, tag.getId())) {
                    logger.debug("标签关联已存在，跳过: artworkId={}, tag={}", artworkId, tag.getName());
                    continue;
                }

                // 3. 创建作品标签关联
                ArtworkTag artworkTag = new ArtworkTag();
                artworkTag.setArtworkId(artworkId);
                artworkTag.setTagId(tag.getId());
                artworkTag.setSource(TagSource.AUTO);
                artworkTag.setConfidence(tagInfo.getConfidence());
                artworkTagRepository.save(artworkTag);

                // 4. 增加标签使用次数
                tag.incrementUsageCount();
                tagRepository.save(tag);

                savedCount++;

                logger.debug("保存自动标签成功: artworkId={}, tag={}, confidence={}",
                        artworkId, tag.getName(), tagInfo.getConfidence());

            } catch (Exception e) {
                logger.error("保存标签失败: artworkId={}, tag={}, error={}",
                        artworkId, tagInfo.getTag(), e.getMessage());
                // 继续处理其他标签
            }
        }

        // 清除作品缓存，使前端轮询能获取到最新的带标签 DTO
        if (savedCount > 0) {
            String cacheKey = ARTWORK_CACHE_PREFIX + artworkId;
            redisTemplate.delete(cacheKey);
            logger.info("已清除作品缓存以刷新标签: artworkId={}", artworkId);
        }

        logger.info("自动标签保存完成: artworkId={}, 成功保存 {} 个标签", artworkId, savedCount);
    }

    /**
     * AI 服务请求 DTO
     */
    public static class TaggingRequest {
        @com.fasterxml.jackson.annotation.JsonProperty("image_url")
        private String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    /**
     * AI 服务响应 DTO
     */
    public static class TaggingResponse {
        private List<TagInfo> tags;
        private Double processingTime;

        public List<TagInfo> getTags() {
            return tags;
        }

        public void setTags(List<TagInfo> tags) {
            this.tags = tags;
        }

        public Double getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(Double processingTime) {
            this.processingTime = processingTime;
        }
    }

    /**
     * 标签信息 DTO
     */
    public static class TagInfo {
        private String tag;

        @com.fasterxml.jackson.annotation.JsonProperty("tag_zh")
        private String tagZh;

        private Float confidence;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getTagZh() {
            return tagZh;
        }

        public void setTagZh(String tagZh) {
            this.tagZh = tagZh;
        }

        public Float getConfidence() {
            return confidence;
        }

        public void setConfidence(Float confidence) {
            this.confidence = confidence;
        }

        /**
         * 获取最终显示名称：优先中文，无中文则用英文
         */
        public String getDisplayName() {
            if (tagZh != null && !tagZh.isEmpty() && !tagZh.equals(tag)) {
                return tagZh;
            }
            return tag;
        }
    }
}
