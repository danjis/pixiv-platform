package com.pixiv.artwork.controller;

import com.pixiv.artwork.service.BrowsingHistoryService;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 浏览记录控制器
 * 提供浏览记录的查询、删除等接口
 */
@Tag(name = "浏览记录", description = "浏览记录查询、删除等接口")
@RestController
@RequestMapping("/api/browsing-history")
public class BrowsingHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(BrowsingHistoryController.class);

    @Autowired
    private BrowsingHistoryService browsingHistoryService;

    /**
     * 获取当前用户的浏览记录
     */
    @Operation(summary = "获取浏览记录", description = "分页获取当前用户的浏览记录，按时间倒序")
    @GetMapping
    public ResponseEntity<Result<PageResult<Map<String, Object>>>> getHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            PageResult<Map<String, Object>> result = browsingHistoryService.getUserHistory(userId, page, size);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            logger.error("获取浏览记录失败: userId={}", userId, e);
            return ResponseEntity.ok(Result.error("获取浏览记录失败"));
        }
    }

    /**
     * 删除单条浏览记录
     */
    @Operation(summary = "删除单条浏览记录")
    @DeleteMapping("/{historyId}")
    public ResponseEntity<Result<Void>> deleteHistory(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("historyId") Long historyId) {
        try {
            browsingHistoryService.deleteHistory(userId, historyId);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            logger.error("删除浏览记录失败: userId={}, historyId={}", userId, historyId, e);
            return ResponseEntity.ok(Result.error("删除失败"));
        }
    }

    /**
     * 清空所有浏览记录
     */
    @Operation(summary = "清空所有浏览记录")
    @DeleteMapping("/clear")
    public ResponseEntity<Result<Void>> clearHistory(
            @RequestHeader("X-User-Id") Long userId) {
        try {
            browsingHistoryService.clearAllHistory(userId);
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            logger.error("清空浏览记录失败: userId={}", userId, e);
            return ResponseEntity.ok(Result.error("清空失败"));
        }
    }
}
