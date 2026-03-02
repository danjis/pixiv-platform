package com.pixiv.commission.service;

import com.pixiv.commission.dto.CommissionPlanDTO;
import com.pixiv.commission.dto.CreateCommissionPlanRequest;
import com.pixiv.commission.entity.CommissionPlan;
import com.pixiv.commission.feign.UserServiceClient;
import com.pixiv.commission.repository.CommissionPlanRepository;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 约稿方案服务
 *
 * 画师可以创建/编辑/删除约稿方案，用户可以查看画师的方案列表
 */
@Service
public class CommissionPlanService {

    private static final Logger logger = LoggerFactory.getLogger(CommissionPlanService.class);

    @Autowired
    private CommissionPlanRepository planRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 获取画师的公开方案列表（用户视角）
     */
    public List<CommissionPlanDTO> getArtistPlans(Long artistId) {
        return planRepository.findByArtistIdAndActiveTrueOrderBySortOrderAsc(artistId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取画师自己的所有方案（包括未启用的）
     */
    public List<CommissionPlanDTO> getMyPlans(Long artistId) {
        return planRepository.findByArtistIdOrderBySortOrderAsc(artistId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取方案详情
     */
    public CommissionPlanDTO getPlan(Long planId) {
        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("方案不存在: " + planId));
        return toDTO(plan);
    }

    /**
     * 创建约稿方案
     */
    @Transactional
    public CommissionPlanDTO createPlan(Long artistId, CreateCommissionPlanRequest request) {
        // 限制最多 10 个方案
        if (planRepository.countByArtistId(artistId) >= 10) {
            throw new IllegalArgumentException("最多创建 10 个约稿方案");
        }

        CommissionPlan plan = CommissionPlan.builder()
                .artistId(artistId)
                .title(request.getTitle())
                .description(request.getDescription())
                .priceStart(request.getPriceStart())
                .priceEnd(request.getPriceEnd())
                .estimatedDays(request.getEstimatedDays())
                .revisionsIncluded(request.getRevisionsIncluded() != null ? request.getRevisionsIncluded() : 3)
                .category(request.getCategory())
                .sampleImages(request.getSampleImages())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(true)
                .build();

        plan = planRepository.save(plan);
        logger.info("约稿方案已创建: id={}, artist={}, title={}", plan.getId(), artistId, request.getTitle());
        return toDTO(plan);
    }

    /**
     * 更新约稿方案
     */
    @Transactional
    public CommissionPlanDTO updatePlan(Long artistId, Long planId, CreateCommissionPlanRequest request) {
        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("方案不存在: " + planId));
        if (!plan.getArtistId().equals(artistId)) {
            throw new IllegalArgumentException("只能编辑自己的方案");
        }

        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setPriceStart(request.getPriceStart());
        plan.setPriceEnd(request.getPriceEnd());
        plan.setEstimatedDays(request.getEstimatedDays());
        if (request.getRevisionsIncluded() != null) {
            plan.setRevisionsIncluded(request.getRevisionsIncluded());
        }
        plan.setCategory(request.getCategory());
        plan.setSampleImages(request.getSampleImages());
        if (request.getSortOrder() != null) {
            plan.setSortOrder(request.getSortOrder());
        }

        plan = planRepository.save(plan);
        logger.info("约稿方案已更新: id={}", planId);
        return toDTO(plan);
    }

    /**
     * 启用/停用方案
     */
    @Transactional
    public CommissionPlanDTO togglePlan(Long artistId, Long planId) {
        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("方案不存在: " + planId));
        if (!plan.getArtistId().equals(artistId)) {
            throw new IllegalArgumentException("只能操作自己的方案");
        }

        plan.setActive(!plan.getActive());
        plan = planRepository.save(plan);
        logger.info("约稿方案状态切换: id={}, active={}", planId, plan.getActive());
        return toDTO(plan);
    }

    /**
     * 删除方案
     */
    @Transactional
    public void deletePlan(Long artistId, Long planId) {
        CommissionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("方案不存在: " + planId));
        if (!plan.getArtistId().equals(artistId)) {
            throw new IllegalArgumentException("只能删除自己的方案");
        }

        planRepository.delete(plan);
        logger.info("约稿方案已删除: id={}", planId);
    }

    // ===================== Private Helpers =====================

    private CommissionPlanDTO toDTO(CommissionPlan plan) {
        UserDTO artist = fetchUser(plan.getArtistId());
        return CommissionPlanDTO.builder()
                .id(plan.getId())
                .artistId(plan.getArtistId())
                .artistName(artist != null ? artist.getUsername() : "用户" + plan.getArtistId())
                .artistAvatar(artist != null ? artist.getAvatarUrl() : null)
                .title(plan.getTitle())
                .description(plan.getDescription())
                .priceStart(plan.getPriceStart())
                .priceEnd(plan.getPriceEnd())
                .estimatedDays(plan.getEstimatedDays())
                .revisionsIncluded(plan.getRevisionsIncluded())
                .category(plan.getCategory())
                .sampleImages(plan.getSampleImages())
                .active(plan.getActive())
                .sortOrder(plan.getSortOrder())
                .createdAt(plan.getCreatedAt())
                .build();
    }

    private UserDTO fetchUser(Long userId) {
        try {
            Result<UserDTO> result = userServiceClient.getUserById(userId);
            return result != null ? result.getData() : null;
        } catch (Exception e) {
            logger.warn("获取用户信息失败: userId={}", userId);
            return null;
        }
    }
}
