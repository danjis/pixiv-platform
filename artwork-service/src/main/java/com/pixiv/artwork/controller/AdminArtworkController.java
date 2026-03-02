package com.pixiv.artwork.controller;

import com.pixiv.artwork.dto.ArtworkDTO;
import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import com.pixiv.artwork.repository.ArtworkRepository;
import com.pixiv.artwork.service.ArtworkService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

        return ResponseEntity.ok(Result.success(stats));
    }
}
