package com.pixiv.commission.controller;

import com.pixiv.commission.dto.CommissionPlanDTO;
import com.pixiv.commission.dto.CreateCommissionPlanRequest;
import com.pixiv.commission.service.CommissionPlanService;
import com.pixiv.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 约稿方案控制器
 *
 * 画师管理约稿方案，用户浏览画师方案
 */
@Tag(name = "约稿方案", description = "画师约稿方案管理")
@RestController
@RequestMapping("/api/commission-plans")
public class CommissionPlanController {

    private static final Logger logger = LoggerFactory.getLogger(CommissionPlanController.class);

    @Autowired
    private CommissionPlanService planService;

    // ===================== 公开接口（用户浏览） =====================

    @Operation(summary = "获取画师的约稿方案", description = "公开接口，查看画师可用的约稿方案")
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<Result<List<CommissionPlanDTO>>> getArtistPlans(
            @PathVariable("artistId") Long artistId) {
        try {
            List<CommissionPlanDTO> plans = planService.getArtistPlans(artistId);
            return ResponseEntity.ok(Result.success(plans));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "获取方案详情")
    @GetMapping("/{planId}")
    public ResponseEntity<Result<CommissionPlanDTO>> getPlan(
            @PathVariable("planId") Long planId) {
        try {
            CommissionPlanDTO plan = planService.getPlan(planId);
            return ResponseEntity.ok(Result.success(plan));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    // ===================== 画师管理接口 =====================

    @Operation(summary = "获取我的约稿方案列表", description = "画师查看自己的所有方案")
    @GetMapping("/mine")
    public ResponseEntity<Result<List<CommissionPlanDTO>>> getMyPlans(
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            List<CommissionPlanDTO> plans = planService.getMyPlans(userId);
            return ResponseEntity.ok(Result.success(plans));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "创建约稿方案")
    @PostMapping
    public ResponseEntity<Result<CommissionPlanDTO>> createPlan(
            @Valid @RequestBody CreateCommissionPlanRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionPlanDTO plan = planService.createPlan(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(plan));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "更新约稿方案")
    @PutMapping("/{planId}")
    public ResponseEntity<Result<CommissionPlanDTO>> updatePlan(
            @PathVariable("planId") Long planId,
            @Valid @RequestBody CreateCommissionPlanRequest request,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionPlanDTO plan = planService.updatePlan(userId, planId, request);
            return ResponseEntity.ok(Result.success(plan));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "启用/停用约稿方案")
    @PutMapping("/{planId}/toggle")
    public ResponseEntity<Result<CommissionPlanDTO>> togglePlan(
            @PathVariable("planId") Long planId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            CommissionPlanDTO plan = planService.togglePlan(userId, planId);
            return ResponseEntity.ok(Result.success(plan));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @Operation(summary = "删除约稿方案")
    @DeleteMapping("/{planId}")
    public ResponseEntity<Result<Void>> deletePlan(
            @PathVariable("planId") Long planId,
            @RequestHeader(value = "X-User-Id") Long userId) {
        try {
            planService.deletePlan(userId, planId);
            return ResponseEntity.ok(Result.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }
}
