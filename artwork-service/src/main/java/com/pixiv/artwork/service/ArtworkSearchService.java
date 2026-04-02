package com.pixiv.artwork.service;

import com.pixiv.artwork.document.ArtworkDocument;
import com.pixiv.artwork.dto.ArtworkDTO;
import com.pixiv.artwork.dto.TagDTO;
import com.pixiv.artwork.dto.ImageDTO;
import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.entity.ArtworkImage;
import com.pixiv.artwork.entity.ArtworkTag;
import com.pixiv.artwork.entity.TagSource;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.repository.ArtworkImageRepository;
import com.pixiv.artwork.repository.ArtworkTagRepository;
import com.pixiv.artwork.repository.TagRepository;
import com.pixiv.artwork.search.ArtworkSearchRepository;
import com.pixiv.common.dto.PageResult;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch._types.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 搜索服务
 *
 * 提供基于 ES 的全文检索、搜索建议、数据索引等功能；
 * 当 ES 不可用时自动降级到 MySQL 模糊查询
 */
@Service
public class ArtworkSearchService {

    private static final Logger logger = LoggerFactory.getLogger(ArtworkSearchService.class);

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ArtworkSearchRepository artworkSearchRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private ArtworkImageRepository artworkImageRepository;

    @Autowired
    private ArtworkTagRepository artworkTagRepository;

    @Autowired
    private TagRepository tagRepository;

    // ==================== 搜索 ====================

    /**
     * 全文检索作品
     *
     * 使用 BM25 算法对 title、description、artistName、tags 四个字段进行加权匹配，
     * 支持标签筛选、AIGC筛选、排序等多维度组合查询
     *
     * @param keyword 搜索关键词
     * @param tags    标签列表（精确匹配，取交集）
     * @param sortBy  排序方式：latest/hottest/most_liked/most_favorited/most_viewed/null(相关性)
     * @param page    页码（从 1 开始）
     * @param size    每页大小
     * @param isAigc  是否AI生成
     * @return 分页搜索结果
     */
    public PageResult<ArtworkDTO> search(String keyword, List<String> tags, String sortBy,
            int page, int size, Boolean isAigc) {
        try {
            NativeQuery query = buildSearchQuery(keyword, tags, sortBy, page, size, isAigc);
            SearchHits<ArtworkDocument> hits = elasticsearchOperations.search(query, ArtworkDocument.class);

            List<ArtworkDTO> dtos = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::convertDocToDTO)
                    .collect(Collectors.toList());

            long total = hits.getTotalHits();
            logger.info("ES搜索完成: keyword={}, 命中={}", keyword, total);

            return new PageResult<>(dtos, total, page, size);

        } catch (Exception e) {
            logger.warn("ES搜索失败，降级到MySQL: {}", e.getMessage());
            return null; // 返回null让调用方降级
        }
    }

    /**
     * 搜索建议（自动补全）
     *
     * 使用 prefix query 对 title.keyword 和 tags 进行前缀匹配，
     * 返回候选词列表供前端下拉展示
     *
     * @param keyword 输入的前缀
     * @param limit   最大返回数量
     * @return 建议词列表
     */
    public List<String> suggest(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // 使用 multi_match prefix 查询获取建议
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .should(s -> s
                                    .prefix(p -> p.field("title.keyword").value(keyword.trim()).caseInsensitive(true)))
                            .should(s -> s.prefix(p -> p.field("tags").value(keyword.trim()).caseInsensitive(true)))
                            .should(s -> s.match(m -> m.field("title").query(keyword.trim()).analyzer("ik_smart")))
                            .minimumShouldMatch("1")))
                    .withFilter(f -> f.term(t -> t.field("status").value("PUBLISHED")))
                    .withPageable(PageRequest.of(0, limit))
                    .build();

            SearchHits<ArtworkDocument> hits = elasticsearchOperations.search(query, ArtworkDocument.class);

            // 收集标题和匹配的标签作为建议
            List<String> suggestions = new ArrayList<>();
            for (SearchHit<ArtworkDocument> hit : hits.getSearchHits()) {
                ArtworkDocument doc = hit.getContent();
                if (doc.getTitle() != null && !suggestions.contains(doc.getTitle())) {
                    suggestions.add(doc.getTitle());
                }
                // 也返回匹配的标签
                if (doc.getTags() != null) {
                    for (String tag : doc.getTags()) {
                        if (tag.toLowerCase().contains(keyword.trim().toLowerCase())
                                && !suggestions.contains(tag)) {
                            suggestions.add(tag);
                        }
                    }
                }
                if (suggestions.size() >= limit)
                    break;
            }
            return suggestions.subList(0, Math.min(suggestions.size(), limit));

        } catch (Exception e) {
            logger.warn("ES搜索建议失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ==================== 索引管理 ====================

    /**
     * 将单个作品索引到 Elasticsearch
     *
     * @param artwork 作品实体（从数据库查询的最新数据）
     */
    public void indexArtwork(Artwork artwork) {
        try {
            ArtworkDocument doc = convertEntityToDocument(artwork);
            artworkSearchRepository.save(doc);
            logger.debug("作品已索引到ES: artworkId={}", artwork.getId());
        } catch (Exception e) {
            logger.error("索引作品到ES失败: artworkId={}, error={}", artwork.getId(), e.getMessage());
        }
    }

    /**
     * 从 ES 中删除作品索引
     *
     * @param artworkId 作品 ID
     */
    public void deleteIndex(Long artworkId) {
        try {
            artworkSearchRepository.deleteById(artworkId);
            logger.debug("作品已从ES删除: artworkId={}", artworkId);
        } catch (Exception e) {
            logger.error("从ES删除作品失败: artworkId={}, error={}", artworkId, e.getMessage());
        }
    }

    /**
     * 全量同步：将所有已发布作品同步到 ES
     * 适用于首次部署或索引重建
     *
     * @return 同步的作品数量
     */
    public int fullSync() {
        logger.info("开始全量同步作品到ES...");
        int count = 0;
        int pageNum = 0;
        int pageSize = 200;

        while (true) {
            var page = artworkRepository.findByStatus(ArtworkStatus.PUBLISHED,
                    PageRequest.of(pageNum, pageSize));
            if (page.isEmpty())
                break;

            List<ArtworkDocument> docs = page.getContent().stream()
                    .map(this::convertEntityToDocument)
                    .collect(Collectors.toList());
            artworkSearchRepository.saveAll(docs);

            count += docs.size();
            pageNum++;
            logger.info("全量同步进度: 已同步 {} 条", count);
        }
        logger.info("全量同步完成: 共 {} 条", count);
        return count;
    }

    // ==================== 私有方法 ====================

    /**
     * 构建 ES 搜索查询
     */
    private NativeQuery buildSearchQuery(String keyword, List<String> tags, String sortBy,
            int page, int size, Boolean isAigc) {
        var builder = NativeQuery.builder();

        // 构建 bool 查询
        builder.withQuery(q -> q.bool(bool -> {
            // 必须是已发布状态
            bool.filter(f -> f.term(t -> t.field("status").value("PUBLISHED")));

            // 关键词搜索：multi_match 跨字段搜索，BM25 自动加权
            if (keyword != null && !keyword.trim().isEmpty()) {
                bool.must(m -> m.multiMatch(mm -> mm
                        .query(keyword.trim())
                        .fields("title^3", "description^1", "artistName^2", "tags^2")
                        .type(TextQueryType.BestFields)
                        .analyzer("ik_smart")
                        .minimumShouldMatch("60%")));
            }

            // 标签筛选（每个标签必须匹配）
            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags) {
                    bool.filter(f -> f.term(t -> t.field("tags").value(tag)));
                }
            }

            // AIGC 筛选
            if (isAigc != null) {
                bool.filter(f -> f.term(t -> t.field("isAigc").value(isAigc)));
            }

            return bool;
        }));

        // 排序
        if (sortBy != null) {
            switch (sortBy) {
                case "latest":
                    builder.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
                    break;
                case "hottest":
                    builder.withSort(s -> s.field(f -> f.field("hotnessScore").order(SortOrder.Desc)));
                    break;
                case "most_liked":
                    builder.withSort(s -> s.field(f -> f.field("likeCount").order(SortOrder.Desc)));
                    break;
                case "most_favorited":
                    builder.withSort(s -> s.field(f -> f.field("favoriteCount").order(SortOrder.Desc)));
                    break;
                case "most_viewed":
                    builder.withSort(s -> s.field(f -> f.field("viewCount").order(SortOrder.Desc)));
                    break;
                default:
                    // 默认按相关性评分
                    break;
            }
        }

        // 分页
        builder.withPageable(PageRequest.of(page - 1, size));

        return builder.build();
    }

    /**
     * 将 Artwork 数据库实体转换为 ES 文档
     */
    private ArtworkDocument convertEntityToDocument(Artwork artwork) {
        ArtworkDocument doc = new ArtworkDocument();
        doc.setId(artwork.getId());
        doc.setTitle(artwork.getTitle());
        doc.setDescription(artwork.getDescription());
        doc.setArtistName(artwork.getArtistName());
        doc.setArtistId(artwork.getArtistId());
        doc.setImageUrl(artwork.getImageUrl());
        doc.setThumbnailUrl(artwork.getThumbnailUrl());
        doc.setStatus(artwork.getStatus() != null ? artwork.getStatus().name() : "PUBLISHED");
        doc.setViewCount(artwork.getViewCount());
        doc.setLikeCount(artwork.getLikeCount());
        doc.setFavoriteCount(artwork.getFavoriteCount());
        doc.setCommentCount(artwork.getCommentCount());
        doc.setHotnessScore(artwork.getHotnessScore());
        doc.setIsAigc(artwork.getIsAigc());
        doc.setCreatedAt(artwork.getCreatedAt());

        // 加载标签名称列表
        List<ArtworkTag> artworkTags = artworkTagRepository.findByArtworkId(artwork.getId());
        List<String> tagNames = artworkTags.stream()
                .map(at -> {
                    var tag = tagRepository.findById(at.getTagId());
                    return tag.map(t -> t.getName()).orElse(null);
                })
                .filter(name -> name != null)
                .collect(Collectors.toList());
        doc.setTags(tagNames);

        return doc;
    }

    /**
     * 将 ES 文档转换为 ArtworkDTO
     * 包含从数据库补充图片和标签明细
     */
    private ArtworkDTO convertDocToDTO(ArtworkDocument doc) {
        ArtworkDTO dto = new ArtworkDTO();
        dto.setId(doc.getId());
        dto.setArtistId(doc.getArtistId());
        dto.setArtistName(doc.getArtistName());
        dto.setTitle(doc.getTitle());
        dto.setDescription(doc.getDescription());
        dto.setImageUrl(doc.getImageUrl());
        dto.setThumbnailUrl(doc.getThumbnailUrl());
        dto.setViewCount(doc.getViewCount());
        dto.setLikeCount(doc.getLikeCount());
        dto.setFavoriteCount(doc.getFavoriteCount());
        dto.setCommentCount(doc.getCommentCount());
        dto.setHotnessScore(doc.getHotnessScore());
        dto.setIsAigc(doc.getIsAigc());
        dto.setCreatedAt(doc.getCreatedAt());

        // 设置状态（从字符串转枚举）
        try {
            dto.setStatus(ArtworkStatus.valueOf(doc.getStatus()));
        } catch (Exception e) {
            dto.setStatus(ArtworkStatus.PUBLISHED);
        }

        // 从数据库补充图片数据
        List<ArtworkImage> images = artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(doc.getId());
        dto.setImages(images.stream()
                .map(img -> new ImageDTO(img.getImageUrl(), img.getOriginalImageUrl(),
                        img.getThumbnailUrl(), img.getSortOrder()))
                .collect(Collectors.toList()));
        dto.setImageCount(images.isEmpty() ? 1 : images.size());

        // 从数据库补充标签详情
        List<ArtworkTag> artworkTags = artworkTagRepository.findByArtworkId(doc.getId());
        List<TagDTO> tagDTOs = artworkTags.stream()
                .map(at -> {
                    var tag = tagRepository.findById(at.getTagId()).orElse(null);
                    if (tag == null)
                        return null;
                    TagDTO tagDTO = new TagDTO();
                    tagDTO.setId(tag.getId());
                    tagDTO.setName(tag.getName());
                    tagDTO.setSource(at.getSource() != null ? at.getSource() : TagSource.MANUAL);
                    return tagDTO;
                })
                .filter(t -> t != null)
                .collect(Collectors.toList());
        dto.setTags(tagDTOs);

        return dto;
    }

    // ==================== 以图搜图 ====================

    /**
     * 以图搜图：基于向量相似度检索
     *
     * 使用 ES kNN 查询对 featureVector 字段进行近似最近邻检索，
     * 返回与输入图片视觉特征最相似的作品列表
     *
     * @param featureVector 查询图片的 256 维特征向量
     * @param size          返回结果数量
     * @return 相似作品列表（按相似度降序）
     */
    public List<ArtworkDTO> searchByImage(float[] featureVector, int size) {
        try {
            // 构建 kNN 向量检索查询
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.scriptScore(ss -> ss
                            .query(innerQ -> innerQ.bool(b -> b
                                    .filter(f -> f.term(t -> t.field("status").value("PUBLISHED")))
                                    .filter(f -> f.exists(e -> e.field("featureVector")))))
                            .script(s -> s.inline(i -> i
                                    .source("cosineSimilarity(params.query_vector, 'featureVector') + 1.0")
                                    .params("query_vector", co.elastic.clients.json.JsonData.of(featureVector))))))
                    .withPageable(PageRequest.of(0, size))
                    .build();

            SearchHits<ArtworkDocument> hits = elasticsearchOperations.search(query, ArtworkDocument.class);

            List<ArtworkDTO> results = new ArrayList<>();
            double MIN_SIMILARITY = 80.0; // 最低相似度阈值（百分比），MobileNetV2特征需要较高阈值
            for (SearchHit<ArtworkDocument> hit : hits.getSearchHits()) {
                // 将 ES 分数转换为相似度百分比（cosineSimilarity 返回 [-1,1]，+1.0 偏移后为 [0,2]）
                double similarity = (hit.getScore() - 1.0) * 100;
                logger.info("以图搜图候选: artworkId={}, similarity={}%", hit.getContent().getId(),
                        String.format("%.2f", similarity));
                if (similarity < MIN_SIMILARITY) {
                    continue; // 跳过相似度过低的结果
                }
                ArtworkDTO dto = convertDocToDTO(hit.getContent());
                dto.setHotnessScore(Math.max(0, Math.round(similarity * 100.0) / 100.0));
                results.add(dto);
            }

            logger.info("以图搜图完成: 返回 {} 条结果（共 {} 条候选）", results.size(), hits.getSearchHits().size());
            return results;

        } catch (Exception e) {
            logger.error("以图搜图失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 为单个作品保存特征向量到 ES
     *
     * @param artworkId     作品 ID
     * @param featureVector 256 维特征向量
     */
    public void updateFeatureVector(Long artworkId, float[] featureVector) {
        try {
            var docOpt = artworkSearchRepository.findById(artworkId);
            if (docOpt.isPresent()) {
                ArtworkDocument doc = docOpt.get();
                doc.setFeatureVector(featureVector);
                artworkSearchRepository.save(doc);
                logger.info("作品特征向量已更新: artworkId={}", artworkId);
            } else {
                logger.warn("ES中未找到作品文档，跳过特征向量更新: artworkId={}", artworkId);
            }
        } catch (Exception e) {
            logger.error("更新特征向量失败: artworkId={}, error={}", artworkId, e.getMessage());
        }
    }

    /**
     * 获取所有已索引作品的 ID 和图片 URL（分页）
     * 用于批量特征提取
     */
    public java.util.Map<Long, String> getAllIndexedArtworkUrls(int page, int size) {
        java.util.Map<Long, String> result = new java.util.LinkedHashMap<>();
        try {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .filter(f -> f.term(t -> t.field("status").value("PUBLISHED")))))
                    .withPageable(PageRequest.of(page, size))
                    .build();

            SearchHits<ArtworkDocument> hits = elasticsearchOperations.search(query, ArtworkDocument.class);
            for (SearchHit<ArtworkDocument> hit : hits.getSearchHits()) {
                ArtworkDocument doc = hit.getContent();
                if (doc.getImageUrl() != null) {
                    result.put(doc.getId(), doc.getImageUrl());
                }
            }
        } catch (Exception e) {
            logger.error("获取已索引作品URL失败: {}", e.getMessage());
        }
        return result;
    }
}
