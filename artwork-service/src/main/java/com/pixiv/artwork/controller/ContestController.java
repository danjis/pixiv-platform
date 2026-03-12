package com.pixiv.artwork.controller;

import com.pixiv.artwork.dto.*;
import com.pixiv.artwork.service.ContestService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 比赛控制器（用户端）
 */
@Tag(name = "比赛接口", description = "比赛浏览、参赛、投票等接口")
@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    /**
     * 获取比赛列表
     */
    @Operation(summary = "获取比赛列表", description = "获取比赛列表，支持按状态筛选")
    @GetMapping
    public ResponseEntity<Result<PageResult<ContestDTO>>> listContests(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<ContestDTO> result = contestService.getContests(status, page - 1, size);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取首页精选参赛作品
     */
    @Operation(summary = "获取首页精选参赛作品", description = "获取进行中/投票中比赛的高分参赛作品")
    @GetMapping("/featured-entries")
    public ResponseEntity<Result<java.util.List<ContestEntryDTO>>> getFeaturedEntries(
            @RequestParam(value = "limit", defaultValue = "8") int limit) {
        java.util.List<ContestEntryDTO> entries = contestService.getFeaturedEntries(limit);
        return ResponseEntity.ok(Result.success(entries));
    }

    /**
     * 获取比赛详情
     */
    @Operation(summary = "获取比赛详情")
    @GetMapping("/{id}")
    public ResponseEntity<Result<ContestDTO>> getContest(@PathVariable("id") Long id) {
        ContestDTO contest = contestService.getContest(id);
        return ResponseEntity.ok(Result.success(contest));
    }

    /**
     * 提交参赛作品
     */
    @Operation(summary = "提交参赛作品", description = "画师提交参赛作品")
    @PostMapping("/{contestId}/entries")
    public ResponseEntity<Result<ContestEntryDTO>> submitEntry(
            @PathVariable("contestId") Long contestId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-Username", required = false) String userName,
            @Valid @RequestBody SubmitEntryRequest request) {
        String artistName = userName != null ? userName : "画师" + userId;
        ContestEntryDTO entry = contestService.submitEntry(contestId, userId, artistName, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(entry));
    }

    /**
     * 获取比赛参赛作品列表（排行榜）
     */
    @Operation(summary = "获取参赛作品列表", description = "获取比赛的参赛作品，按评分排序")
    @GetMapping("/{contestId}/entries")
    public ResponseEntity<Result<PageResult<ContestEntryDTO>>> getEntries(
            @PathVariable("contestId") Long contestId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PageResult<ContestEntryDTO> result = contestService.getEntries(contestId, page - 1, size, userId);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 投票
     */
    @Operation(summary = "投票评分", description = "对参赛作品进行评分(1-10分)")
    @PostMapping("/{contestId}/vote")
    public ResponseEntity<Result<ContestEntryDTO>> vote(
            @PathVariable("contestId") Long contestId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody VoteRequest request) {
        ContestEntryDTO entry = contestService.vote(contestId, userId, request);
        return ResponseEntity.ok(Result.success(entry));
    }

    /**
     * 获取参赛作品的投票评价列表
     */
    @Operation(summary = "获取投票评价列表")
    @GetMapping("/entries/{entryId}/votes")
    public ResponseEntity<Result<PageResult<ContestVoteDTO>>> getVotes(
            @PathVariable("entryId") Long entryId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        PageResult<ContestVoteDTO> result = contestService.getVotes(entryId, page - 1, size);
        return ResponseEntity.ok(Result.success(result));
    }
}
