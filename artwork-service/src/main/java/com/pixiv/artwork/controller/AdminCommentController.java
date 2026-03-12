package com.pixiv.artwork.controller;

import com.pixiv.artwork.entity.Comment;
import com.pixiv.artwork.repository.CommentRepository;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理员评论管理控制器
 * 提供评论的查看、搜索和删除功能
 */
@Tag(name = "管理员-评论管理", description = "管理员评论审核、删除接口")
@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private static final Logger logger = LoggerFactory.getLogger(AdminCommentController.class);

    @Autowired
    private CommentRepository commentRepository;

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * 获取所有评论列表（分页）
     */
    @Operation(summary = "获取评论列表（管理员）")
    @GetMapping
    public ResponseEntity<Result<PageResult<Map<String, Object>>>> listComments(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限访问"));
        }

        Page<Comment> commentPage;
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (keyword != null && !keyword.trim().isEmpty()) {
            commentPage = commentRepository.findByContentContaining(keyword.trim(), pageable);
        } else {
            commentPage = commentRepository.findAll(pageable);
        }

        List<Map<String, Object>> records = commentPage.getContent().stream()
                .map(c -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", c.getId());
                    map.put("artworkId", c.getArtworkId());
                    map.put("userId", c.getUserId());
                    map.put("parentId", c.getParentId());
                    map.put("content", c.getContent());
                    map.put("createdAt", c.getCreatedAt());
                    return map;
                })
                .toList();

        PageResult<Map<String, Object>> result = new PageResult<>(
                records, commentPage.getTotalElements(), page, size);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 删除评论（管理员）
     */
    @Operation(summary = "删除评论（管理员）")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Result<Void>> deleteComment(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String adminId,
            @PathVariable("commentId") Long commentId) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限访问"));
        }

        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Result.error(404, "评论不存在"));
        }

        commentRepository.deleteById(commentId);
        logger.info("管理员[{}]删除了评论[{}]", adminId, commentId);
        return ResponseEntity.ok(Result.success(null));
    }

    /**
     * 获取评论统计
     */
    @Operation(summary = "获取评论统计")
    @GetMapping("/stats")
    public ResponseEntity<Result<Map<String, Object>>> getCommentStats(
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!isAdmin(role)) {
            return ResponseEntity.status(403).body(Result.error(403, "无权限访问"));
        }

        long totalComments = commentRepository.count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalComments", totalComments);
        return ResponseEntity.ok(Result.success(stats));
    }
}
