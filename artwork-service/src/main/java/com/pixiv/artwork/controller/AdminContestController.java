package com.pixiv.artwork.controller;

import com.pixiv.artwork.dto.*;
import com.pixiv.artwork.service.ContestService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员比赛管理控制器
 */
@Tag(name = "管理员-比赛管理", description = "管理员比赛创建、管理接口")
@RestController
@RequestMapping("/api/admin/contests")
public class AdminContestController {

    private static final Logger logger = LoggerFactory.getLogger(AdminContestController.class);

    private final ContestService contestService;

    public AdminContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * 获取所有比赛列表
     */
    @Operation(summary = "获取比赛列表（管理员）")
    @GetMapping
    public ResponseEntity<Result<PageResult<ContestDTO>>> listContests(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        PageResult<ContestDTO> result = contestService.getAllContests(page - 1, size);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取比赛详情
     */
    @Operation(summary = "获取比赛详情（管理员）")
    @GetMapping("/{id}")
    public ResponseEntity<Result<ContestDTO>> getContest(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("id") Long id) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        ContestDTO contest = contestService.getContest(id);
        return ResponseEntity.ok(Result.success(contest));
    }

    /**
     * 创建比赛
     */
    @Operation(summary = "创建比赛")
    @PostMapping
    public ResponseEntity<Result<ContestDTO>> createContest(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody ContestRequest request) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        ContestDTO contest = contestService.createContest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(contest));
    }

    /**
     * 更新比赛
     */
    @Operation(summary = "更新比赛")
    @PutMapping("/{id}")
    public ResponseEntity<Result<ContestDTO>> updateContest(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("id") Long id,
            @Valid @RequestBody ContestRequest request) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        ContestDTO contest = contestService.updateContest(id, request);
        return ResponseEntity.ok(Result.success(contest));
    }

    /**
     * 更新比赛状态
     */
    @Operation(summary = "更新比赛状态")
    @PutMapping("/{id}/status")
    public ResponseEntity<Result<ContestDTO>> updateStatus(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        String status = body.get("status");
        ContestDTO contest = contestService.updateContestStatus(id, status);
        return ResponseEntity.ok(Result.success(contest));
    }

    /**
     * 删除比赛
     */
    @Operation(summary = "删除比赛")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteContest(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("id") Long id) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        contestService.deleteContest(id);
        return ResponseEntity.ok(Result.success("比赛已删除", null));
    }

    /**
     * 获取比赛参赛作品列表
     */
    @Operation(summary = "获取参赛作品列表（管理员）")
    @GetMapping("/{contestId}/entries")
    public ResponseEntity<Result<PageResult<ContestEntryDTO>>> getEntries(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("contestId") Long contestId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        PageResult<ContestEntryDTO> result = contestService.getEntries(contestId, page - 1, size, null);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 审核参赛作品
     */
    @Operation(summary = "审核参赛作品")
    @PutMapping("/entries/{entryId}/review")
    public ResponseEntity<Result<ContestEntryDTO>> reviewEntry(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("entryId") Long entryId,
            @RequestBody Map<String, String> body) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        String status = body.get("status");
        ContestEntryDTO entry = contestService.reviewEntry(entryId, status);
        return ResponseEntity.ok(Result.success(entry));
    }

    /**
     * 手动计算排名
     */
    @Operation(summary = "计算排名")
    @PostMapping("/{id}/calculate-rankings")
    public ResponseEntity<Result<Void>> calculateRankings(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable("id") Long id) {
        if (!isAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Result.error("无权限"));
        }
        contestService.calculateRankings(id);
        return ResponseEntity.ok(Result.success("排名计算完成", null));
    }
}
