package com.pixiv.artwork.controller;

import com.pixiv.artwork.dto.ArtworkDTO;
import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.service.ArtworkService;
import com.pixiv.artwork.service.ArtworkSearchService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理员作品管理控制器
 * 提供管理员对作品进行查看、搜索、删除等管理操作的接口
 */
@Tag(name = "管理员-作品管理", description = "管理员作品审核、管理接口")
@RestController
@RequestMapping("/api/admin/artworks")
public class AdminArtworkController {

    private static final Logger logger = LoggerFactory.getLogger(AdminArtworkController.class);

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private ArtworkService artworkService;

    @Autowired
    private ArtworkSearchService artworkSearchService;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * 获取所有作品列表（分页、可搜索、可按状态筛选）
     */
    @Operation(summary = "获取作品列表（管理员）", description = "管理员获取所有作品，支持分页、搜索、状态筛选")
    @GetMapping
    public ResponseEntity<Result<PageResult<ArtworkDTO>>> listArtworks(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限访问"));
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Artwork> artworkPage;

        // 解析状态筛选
        ArtworkStatus artworkStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                artworkStatus = ArtworkStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 无效的状态值，忽略筛选
            }
        }

        // 根据条件查询
        if (keyword != null && !keyword.trim().isEmpty() && artworkStatus != null) {
            artworkPage = artworkRepository.searchByKeyword(keyword.trim(), artworkStatus, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            artworkPage = artworkRepository.searchByKeyword(keyword.trim(), ArtworkStatus.PUBLISHED, pageable);
        } else if (artworkStatus != null) {
            artworkPage = artworkRepository.findByStatus(artworkStatus, pageable);
        } else {
            artworkPage = artworkRepository.findAll(pageable);
        }

        List<ArtworkDTO> dtos = artworkService.toArtworkDTOList(artworkPage.getContent());

        PageResult<ArtworkDTO> pageResult = new PageResult<>(
                dtos,
                artworkPage.getTotalElements(),
                page,
                size);

        return ResponseEntity.ok(Result.success(pageResult));
    }

    /**
     * 获取作品详情（管理员）
     */
    @Operation(summary = "获取作品详情（管理员）", description = "管理员可查看任意状态的作品详情")
    @GetMapping("/{artworkId}")
    public ResponseEntity<Result<ArtworkDTO>> getArtwork(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("artworkId") Long artworkId) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限访问"));
        }

        Optional<Artwork> artworkOpt = artworkRepository.findById(artworkId);
        if (artworkOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Result.error(404, "作品不存在"));
        }

        List<ArtworkDTO> dtosForDetail = artworkService.toArtworkDTOList(List.of(artworkOpt.get()));
        return ResponseEntity.ok(Result.success(dtosForDetail.get(0)));
    }

    /**
     * 删除作品（管理员）- 软删除，将状态改为 DELETED
     */
    @Operation(summary = "删除作品（管理员）", description = "管理员删除作品（软删除）")
    @DeleteMapping("/{artworkId}")
    public ResponseEntity<Result<Void>> deleteArtwork(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String adminId,
            @PathVariable("artworkId") Long artworkId) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限访问"));
        }

        Optional<Artwork> artworkOpt = artworkRepository.findById(artworkId);
        if (artworkOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Result.error(404, "作品不存在"));
        }

        Artwork artwork = artworkOpt.get();
        if (artwork.getStatus() == ArtworkStatus.DELETED) {
            return ResponseEntity.badRequest().body(Result.error(400, "作品已被删除"));
        }

        artwork.setStatus(ArtworkStatus.DELETED);
        artworkRepository.save(artwork);

        logger.info("管理员[{}]删除了作品[{}]: {}", adminId, artworkId, artwork.getTitle());
        return ResponseEntity.ok(Result.success(null));
    }

    /**
     * 恢复作品（管理员）- 将已删除的作品恢复为已发布状态
     */
    @Operation(summary = "恢复作品（管理员）", description = "管理员恢复已删除的作品")
    @PutMapping("/{artworkId}/restore")
    public ResponseEntity<Result<Void>> restoreArtwork(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String adminId,
            @PathVariable("artworkId") Long artworkId) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限访问"));
        }

        Optional<Artwork> artworkOpt = artworkRepository.findById(artworkId);
        if (artworkOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Result.error(404, "作品不存在"));
        }

        Artwork artwork = artworkOpt.get();
        if (artwork.getStatus() != ArtworkStatus.DELETED) {
            return ResponseEntity.badRequest().body(Result.error(400, "该作品未被删除，无需恢复"));
        }

        artwork.setStatus(ArtworkStatus.PUBLISHED);
        artworkRepository.save(artwork);

        logger.info("管理员[{}]恢复了作品[{}]: {}", adminId, artworkId, artwork.getTitle());
        return ResponseEntity.ok(Result.success(null));
    }

    /**
     * 获取作品统计信息（管理员）
     */
    @Operation(summary = "获取作品统计", description = "获取作品相关的统计数据")
    @GetMapping("/stats")
    public ResponseEntity<Result<Map<String, Object>>> getArtworkStats(
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限访问"));
        }

        long totalArtworks = artworkRepository.count();
        long publishedCount = artworkRepository.findByStatus(ArtworkStatus.PUBLISHED, PageRequest.of(0, 1))
                .getTotalElements();
        long deletedCount = artworkRepository.findByStatus(ArtworkStatus.DELETED, PageRequest.of(0, 1))
                .getTotalElements();
        long draftCount = artworkRepository.findByStatus(ArtworkStatus.DRAFT, PageRequest.of(0, 1)).getTotalElements();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalArtworks", totalArtworks);
        stats.put("publishedCount", publishedCount);
        stats.put("deletedCount", deletedCount);
        stats.put("draftCount", draftCount);
        stats.put("totalViews", artworkRepository.sumViewCount());
        stats.put("totalLikes", artworkRepository.sumLikeCount());
        stats.put("totalFavorites", artworkRepository.sumFavoriteCount());
        stats.put("totalComments", artworkRepository.sumCommentCount());

        return ResponseEntity.ok(Result.success(stats));
    }

    /**
     * 全量同步作品到 Elasticsearch
     * 
     * 将数据库中所有已发布作品同步到 ES 索引，适用于首次部署或索引重建
     */
    @Operation(summary = "全量同步ES索引", description = "将所有已发布作品全量同步到 Elasticsearch，用于首次部署或索引重建")
    @PostMapping("/es/full-sync")
    public ResponseEntity<Result<Map<String, Object>>> fullSyncEs(
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error("需要管理员权限"));
        }

        try {
            int count = artworkSearchService.fullSync();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("syncedCount", count);
            result.put("message", "全量同步完成");
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("ES全量同步失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Result.error("同步失败: " + e.getMessage()));
        }
    }

    /**
     * 批量提取作品特征向量
     *
     * 遍历所有已索引的作品，调用 AI 服务提取特征向量并更新 ES，
     * 用于启用以图搜图功能
     */
    @Operation(summary = "批量提取特征向量", description = "为所有已索引作品提取 AI 特征向量，启用以图搜图功能")
    @PostMapping("/es/extract-features")
    public ResponseEntity<Result<Map<String, Object>>> batchExtractFeatures(
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error("需要管理员权限"));
        }

        try {
            int count = 0;
            int failed = 0;
            int pageNum = 0;
            int pageSize = 50;
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

            while (true) {
                var page = artworkSearchService.getAllIndexedArtworkUrls(pageNum, pageSize);
                if (page.isEmpty())
                    break;

                for (var entry : page.entrySet()) {
                    Long artworkId = entry.getKey();
                    String imageUrl = entry.getValue();
                    try {
                        // 调用 AI 服务提取特征
                        Map<String, Object> request = new java.util.HashMap<>();
                        request.put("image_url", imageUrl);
                        var response = restTemplate.postForObject(
                                aiServiceUrl + "/api/extract-features", request, Map.class);

                        if (response != null && response.containsKey("feature_vector")) {
                            java.util.List<Number> vectorList = (java.util.List<Number>) response.get("feature_vector");
                            float[] vector = new float[vectorList.size()];
                            for (int i = 0; i < vectorList.size(); i++) {
                                vector[i] = vectorList.get(i).floatValue();
                            }
                            artworkSearchService.updateFeatureVector(artworkId, vector);
                            count++;
                        }
                    } catch (Exception e) {
                        logger.warn("作品特征提取失败: artworkId={}, error={}", artworkId, e.getMessage());
                        failed++;
                    }
                }
                pageNum++;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("extractedCount", count);
            result.put("failedCount", failed);
            result.put("message", "特征提取完成");
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("批量特征提取失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Result.error("提取失败: " + e.getMessage()));
        }
    }

    /**
     * 批量翻译标签（填充 nameZh 字段）
     */
    @Operation(summary = "批量翻译标签", description = "调用 AI 服务翻译所有没有中文名的标签")
    @PostMapping("/tags/translate")
    public ResponseEntity<Result<?>> batchTranslateTags() {
        try {
            // 获取所有没有中文翻译的标签
            var allTags = tagRepository.findAll();
            var needTranslate = allTags.stream()
                    .filter(t -> t.getNameZh() == null || t.getNameZh().isEmpty())
                    .map(com.pixiv.artwork.entity.Tag::getName)
                    .toList();

            if (needTranslate.isEmpty()) {
                return ResponseEntity.ok(Result.success(Map.of("translatedCount", 0, "message", "所有标签已有中文翻译")));
            }

            // 调用 AI 服务批量翻译
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("tags", needTranslate);
            var response = restTemplate.postForObject(
                    aiServiceUrl + "/api/tags/translate", request, Map.class);

            int translated = 0;
            if (response != null && response.containsKey("translations")) {
                Map<String, String> translations = (Map<String, String>) response.get("translations");
                for (var tag : allTags) {
                    String zh = translations.get(tag.getName());
                    if (zh != null && !zh.isEmpty()) {
                        tag.setNameZh(zh);
                        tagRepository.save(tag);
                        translated++;
                    }
                }
            }

            return ResponseEntity.ok(Result.success(Map.of(
                    "totalTags", allTags.size(),
                    "translatedCount", translated,
                    "message", "标签翻译完成")));
        } catch (Exception e) {
            logger.error("批量翻译标签失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Result.error("翻译失败: " + e.getMessage()));
        }
    }

    @Autowired
    private com.pixiv.artwork.repository.TagRepository tagRepository;
}
