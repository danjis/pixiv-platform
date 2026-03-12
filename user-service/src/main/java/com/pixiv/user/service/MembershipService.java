package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.dto.Result;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.user.dto.MembershipDTO;
import com.pixiv.user.entity.UserMembership;
import com.pixiv.user.entity.MembershipLevel;
import com.pixiv.user.feign.CommissionServiceClient;
import com.pixiv.user.repository.UserMembershipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MembershipService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipService.class);

    private final UserMembershipRepository userMembershipRepository;
    private final CommissionServiceClient commissionServiceClient;

    public MembershipService(UserMembershipRepository userMembershipRepository,
            CommissionServiceClient commissionServiceClient) {
        this.userMembershipRepository = userMembershipRepository;
        this.commissionServiceClient = commissionServiceClient;
    }

    @Transactional
    public MembershipDTO getMembership(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        UserMembership membership = userMembershipRepository.findByUserId(userId).orElse(null);
        if (membership == null) {
            membership = new UserMembership();
            membership.setUserId(userId);
            membership.setLevel(MembershipLevel.NORMAL);
            membership.setCreatedAt(LocalDateTime.now());
            membership.setUpdatedAt(LocalDateTime.now());
            membership = userMembershipRepository.save(membership);
            logger.info("为用户 {} 创建默认NORMAL会员记录", userId);
        }

        // 检查VIP/SVIP是否已过期，过期则降级为NORMAL
        if (membership.getLevel() != MembershipLevel.NORMAL
                && membership.getExpireTime() != null
                && membership.getExpireTime().isBefore(LocalDateTime.now())) {
            logger.info("用户 {} 的 {} 会员已过期，降级为NORMAL", userId, membership.getLevel());
            membership.setLevel(MembershipLevel.NORMAL);
            membership.setExpireTime(null);
            membership.setUpdatedAt(LocalDateTime.now());
            membership = userMembershipRepository.save(membership);
        }

        return new MembershipDTO(membership);
    }

    public MembershipLevel getEffectiveLevel(Long userId) {
        if (userId == null) {
            return MembershipLevel.NORMAL;
        }

        UserMembership membership = userMembershipRepository.findByUserId(userId).orElse(null);
        if (membership == null) {
            return MembershipLevel.NORMAL;
        }

        if (membership.getLevel() != MembershipLevel.NORMAL
                && membership.getExpireTime() != null
                && membership.getExpireTime().isBefore(LocalDateTime.now())) {
            return MembershipLevel.NORMAL;
        }

        return membership.getLevel();
    }

    @Transactional
    public MembershipDTO upgradeMembership(Long userId, String level, int durationDays) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (durationDays <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "会员时长必须大于0天");
        }

        MembershipLevel targetLevel;
        try {
            targetLevel = MembershipLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的会员等级: " + level);
        }

        if (targetLevel == MembershipLevel.NORMAL) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能升级为NORMAL等级");
        }

        UserMembership membership = userMembershipRepository.findByUserId(userId).orElse(null);
        if (membership == null) {
            membership = new UserMembership();
            membership.setUserId(userId);
            membership.setLevel(targetLevel);
            membership.setExpireTime(LocalDateTime.now().plusDays(durationDays));
            membership.setCreatedAt(LocalDateTime.now());
            membership.setUpdatedAt(LocalDateTime.now());
        } else {
            // 如果当前已是VIP/SVIP且未过期，则延长时长
            if (membership.getLevel() != MembershipLevel.NORMAL
                    && membership.getExpireTime() != null
                    && membership.getExpireTime().isAfter(LocalDateTime.now())) {
                membership.setLevel(targetLevel);
                membership.setExpireTime(membership.getExpireTime().plusDays(durationDays));
                logger.info("用户 {} 会员延长 {} 天，新到期时间: {}", userId, durationDays, membership.getExpireTime());
            } else {
                membership.setLevel(targetLevel);
                membership.setExpireTime(LocalDateTime.now().plusDays(durationDays));
            }
            membership.setUpdatedAt(LocalDateTime.now());
        }

        membership = userMembershipRepository.save(membership);
        logger.info("用户 {} 会员升级为 {}，到期时间: {}", userId, targetLevel, membership.getExpireTime());

        return new MembershipDTO(membership);
    }

    /**
     * 管理员查看会员列表（分页）
     */
    public Map<String, Object> listMembers(int page, int size, String levelFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<UserMembership> pageResult;

        if (levelFilter != null && !levelFilter.isEmpty()) {
            try {
                MembershipLevel level = MembershipLevel.valueOf(levelFilter.toUpperCase());
                if (level == MembershipLevel.NORMAL) {
                    // 筛选普通用户没太大意义，但允许
                    pageResult = userMembershipRepository.findByLevel(level, pageable);
                } else {
                    pageResult = userMembershipRepository.findByLevel(level, pageable);
                }
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的会员等级: " + levelFilter);
            }
        } else {
            // 默认只显示VIP和SVIP会员
            pageResult = userMembershipRepository.findByLevelNot(MembershipLevel.NORMAL, pageable);
        }

        List<MembershipDTO> dtos = pageResult.getContent().stream()
                .map(MembershipDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", dtos);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", pageResult.getTotalPages());
        // 统计数据
        result.put("vipCount", userMembershipRepository.countByLevelNot(MembershipLevel.NORMAL));
        return result;
    }

    public int getPointsMultiplier(MembershipLevel level) {
        if (level == null) {
            return 1;
        }
        switch (level) {
            case VIP:
                return 2;
            case SVIP:
                return 3;
            default:
                return 1;
        }
    }

    // =========== 定时任务 ===========

    /**
     * 每月1日凌晨1点自动为VIP/SVIP会员发放月度优惠券
     * VIP: 5元优惠券
     * SVIP: 15元优惠券
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void grantMonthlyCoupons() {
        logger.info("===== 开始发放会员月度优惠券 =====");

        List<UserMembership> activeMembers = userMembershipRepository.findActiveMembers(LocalDateTime.now());
        int vipCount = 0, svipCount = 0, failCount = 0;

        for (UserMembership member : activeMembers) {
            String couponName;
            BigDecimal amount;

            if (member.getLevel() == MembershipLevel.SVIP) {
                couponName = "SVIP月度专属15元优惠券";
                amount = new BigDecimal("15");
                svipCount++;
            } else if (member.getLevel() == MembershipLevel.VIP) {
                couponName = "VIP月度专属5元优惠券";
                amount = new BigDecimal("5");
                vipCount++;
            } else {
                continue;
            }

            try {
                Map<String, Object> request = new LinkedHashMap<>();
                request.put("userId", member.getUserId());
                request.put("couponName", couponName);
                request.put("amount", amount);
                request.put("validDays", 30);
                Result<Map<String, Object>> result = commissionServiceClient.createCouponForUser(request);
                if (result == null || result.getCode() != 200) {
                    logger.warn("发放月度优惠券失败: userId={}, result={}", member.getUserId(), result);
                    failCount++;
                }
            } catch (Exception e) {
                logger.error("发放月度优惠券异常: userId={}", member.getUserId(), e);
                failCount++;
            }
        }

        logger.info("===== 月度优惠券发放完成: VIP={}人, SVIP={}人, 失败={}次 =====", vipCount, svipCount, failCount);
    }
}
