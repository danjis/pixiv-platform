package com.pixiv.commission.service;

import com.pixiv.commission.entity.*;
import com.pixiv.commission.repository.CouponRepository;
import com.pixiv.commission.repository.UserCouponRepository;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.common.constant.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券服务
 * 提供优惠券的创建、领取、使用、查询等功能
 */
@Service
public class CouponService {

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    // =========== 管理端接口 ===========

    /**
     * 创建优惠券（管理端）
     */
    @Transactional
    public Coupon createCoupon(String name, String description, CouponType type,
            BigDecimal discountValue, BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount, Integer totalQuantity,
            LocalDateTime startTime, LocalDateTime endTime) {
        return createCoupon(name, description, type, discountValue, minOrderAmount,
                maxDiscountAmount, totalQuantity, startTime, endTime, "PUBLIC");
    }

    /**
     * 创建优惠券（管理端，含发放方式）
     */
    @Transactional
    public Coupon createCoupon(String name, String description, CouponType type,
            BigDecimal discountValue, BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount, Integer totalQuantity,
            LocalDateTime startTime, LocalDateTime endTime, String distributionType) {
        // 生成唯一代码
        String code = generateCouponCode();

        Coupon coupon = Coupon.builder()
                .code(code)
                .name(name)
                .description(description)
                .type(type)
                .discountValue(discountValue)
                .minOrderAmount(minOrderAmount != null ? minOrderAmount : BigDecimal.ZERO)
                .maxDiscountAmount(maxDiscountAmount)
                .totalQuantity(totalQuantity)
                .startTime(startTime)
                .endTime(endTime)
                .status(CouponStatus.ACTIVE)
                .distributionType(distributionType != null ? distributionType : "PUBLIC")
                .build();

        coupon = couponRepository.save(coupon);
        logger.info("创建优惠券: id={}, code={}, name={}, distributionType={}", coupon.getId(), coupon.getCode(),
                coupon.getName(), coupon.getDistributionType());
        return coupon;
    }

    /**
     * 获取所有优惠券列表（管理端）
     */
    public PageResult<Map<String, Object>> getAllCoupons(int page, int size) {
        Page<Coupon> couponPage = couponRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        List<Map<String, Object>> records = couponPage.getContent().stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
        return new PageResult<>(records, couponPage.getTotalElements(), page + 1, size);
    }

    /**
     * 停用 / 启用优惠券（管理端）
     */
    @Transactional
    public void toggleCouponStatus(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券不存在"));
        if (coupon.getStatus() == CouponStatus.ACTIVE) {
            coupon.setStatus(CouponStatus.INACTIVE);
        } else if (coupon.getStatus() == CouponStatus.INACTIVE) {
            coupon.setStatus(CouponStatus.ACTIVE);
        }
        couponRepository.save(coupon);
        logger.info("切换优惠券状态: id={}, newStatus={}", couponId, coupon.getStatus());
    }

    // =========== 用户端接口 ===========

    /**
     * 获取可领取的优惠券列表（用户端）
     * 仅展示 distributionType=PUBLIC 的优惠券，CODE_ONLY 类型仅通过券码兑换
     */
    public PageResult<Map<String, Object>> getAvailableCoupons(Long userId, int page, int size) {
        Page<Coupon> couponPage = couponRepository.findByStatusAndEndTimeAfterOrderByCreatedAtDesc(
                CouponStatus.ACTIVE, LocalDateTime.now(), PageRequest.of(page, size));

        // 查询用户已领取的优惠券 ID
        Set<Long> claimedCouponIds = new HashSet<>();
        if (userId != null) {
            userCouponRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 1000))
                    .getContent().forEach(uc -> claimedCouponIds.add(uc.getCouponId()));
        }

        List<Map<String, Object>> records = couponPage.getContent().stream()
                // 只展示公开发放的优惠券，CODE_ONLY 类型需要通过券码兑换
                .filter(c -> !"CODE_ONLY".equals(c.getDistributionType()))
                .map(c -> {
                    Map<String, Object> map = convertToMap(c);
                    map.put("claimed", claimedCouponIds.contains(c.getId()));
                    return map;
                })
                .collect(Collectors.toList());

        return new PageResult<>(records, couponPage.getTotalElements(), page + 1, size);
    }

    /**
     * 用户领取优惠券
     */
    @Transactional
    public void claimCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券不存在"));

        if (!coupon.isClaimable()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "优惠券不可领取（已过期、已停用或已领完）");
        }

        // 检查是否已领取
        if (userCouponRepository.findByUserIdAndCouponId(userId, couponId).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_OPERATION, "你已经领取过该优惠券");
        }

        // 创建用户优惠券记录
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(UserCouponStatus.UNUSED)
                .build();
        userCouponRepository.save(userCoupon);

        // 增加已领取数量
        coupon.setClaimedQuantity(coupon.getClaimedQuantity() + 1);
        couponRepository.save(coupon);

        logger.info("用户领取优惠券: userId={}, couponId={}", userId, couponId);
    }

    /**
     * 通过优惠码领取优惠券
     */
    @Transactional
    public void claimByCode(Long userId, String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券代码无效"));
        claimCoupon(userId, coupon.getId());
    }

    /**
     * 获取用户的优惠券列表
     */
    public PageResult<Map<String, Object>> getUserCoupons(Long userId, String status, int page, int size) {
        Page<UserCoupon> ucPage;
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            UserCouponStatus ucStatus = UserCouponStatus.valueOf(status.toUpperCase());
            ucPage = userCouponRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, ucStatus,
                    PageRequest.of(page, size));
        } else {
            ucPage = userCouponRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        }

        // 批量加载优惠券信息
        Set<Long> couponIds = ucPage.getContent().stream()
                .map(UserCoupon::getCouponId)
                .collect(Collectors.toSet());
        Map<Long, Coupon> couponMap = couponRepository.findAllById(couponIds).stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));

        List<Map<String, Object>> records = ucPage.getContent().stream()
                .map(uc -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", uc.getId());
                    map.put("couponId", uc.getCouponId());
                    map.put("status", uc.getStatus().name());
                    map.put("usedAt", uc.getUsedAt());
                    map.put("commissionId", uc.getCommissionId());
                    map.put("claimedAt", uc.getCreatedAt());

                    Coupon coupon = couponMap.get(uc.getCouponId());
                    if (coupon != null) {
                        map.put("name", coupon.getName());
                        map.put("description", coupon.getDescription());
                        map.put("type", coupon.getType().name());
                        map.put("discountValue", coupon.getDiscountValue());
                        map.put("minOrderAmount", coupon.getMinOrderAmount());
                        map.put("maxDiscountAmount", coupon.getMaxDiscountAmount());
                        map.put("startTime", coupon.getStartTime());
                        map.put("endTime", coupon.getEndTime());
                        map.put("code", coupon.getCode());
                        // 检查是否已过期（用户优惠券可能状态未同步）
                        if (uc.getStatus() == UserCouponStatus.UNUSED
                                && coupon.getEndTime().isBefore(LocalDateTime.now())) {
                            map.put("status", "EXPIRED");
                        }
                    }
                    return map;
                })
                .collect(Collectors.toList());

        return new PageResult<>(records, ucPage.getTotalElements(), page + 1, size);
    }

    /**
     * 获取用户某笔订单可用的优惠券列表
     */
    public List<Map<String, Object>> getAvailableForOrder(Long userId, BigDecimal orderAmount) {
        List<UserCoupon> available = userCouponRepository.findAvailableForOrder(userId, orderAmount);
        return available.stream()
                .map(uc -> {
                    Coupon c = uc.getCoupon();
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("userCouponId", uc.getId());
                    map.put("couponId", c.getId());
                    map.put("name", c.getName());
                    map.put("type", c.getType().name());
                    map.put("discountValue", c.getDiscountValue());
                    map.put("discountAmount", c.calculateDiscount(orderAmount));
                    map.put("minOrderAmount", c.getMinOrderAmount());
                    map.put("maxDiscountAmount", c.getMaxDiscountAmount());
                    map.put("endTime", c.getEndTime());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 使用优惠券（在支付时调用）
     * 
     * @return 折扣金额
     */
    @Transactional
    public BigDecimal useCoupon(Long userId, Long userCouponId, Long commissionId, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券不存在"));

        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权使用该优惠券");
        }

        if (userCoupon.getStatus() != UserCouponStatus.UNUSED) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "优惠券已使用或已过期");
        }

        Coupon coupon = couponRepository.findById(userCoupon.getCouponId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券信息不存在"));

        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "优惠券已过期");
        }

        BigDecimal discount = coupon.calculateDiscount(orderAmount);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "订单金额不满足优惠券使用条件");
        }

        // 标记为已使用
        userCoupon.setStatus(UserCouponStatus.USED);
        userCoupon.setUsedAt(LocalDateTime.now());
        userCoupon.setCommissionId(commissionId);
        userCouponRepository.save(userCoupon);

        // 增加优惠券使用量
        coupon.setUsedQuantity(coupon.getUsedQuantity() + 1);
        couponRepository.save(coupon);

        logger.info("用户使用优惠券: userId={}, couponId={}, commissionId={}, discount={}",
                userId, coupon.getId(), commissionId, discount);

        return discount;
    }

    /**
     * 归还优惠券（取消订单时调用）
     */
    @Transactional
    public void returnCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId).orElse(null);
        if (userCoupon == null || userCoupon.getStatus() != UserCouponStatus.USED)
            return;
        userCoupon.setStatus(UserCouponStatus.UNUSED);
        userCoupon.setUsedAt(null);
        userCoupon.setCommissionId(null);
        userCouponRepository.save(userCoupon);

        Coupon coupon = couponRepository.findById(userCoupon.getCouponId()).orElse(null);
        if (coupon != null && coupon.getUsedQuantity() > 0) {
            coupon.setUsedQuantity(coupon.getUsedQuantity() - 1);
            couponRepository.save(coupon);
        }
        logger.info("归还优惠券: userCouponId={}", userCouponId);
    }

    // =========== 定时任务 ===========

    /**
     * 定时更新过期优惠券状态（每小时执行一次）
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void expireCoupons() {
        List<Coupon> expired = couponRepository.findByStatusAndEndTimeBefore(CouponStatus.ACTIVE, LocalDateTime.now());
        for (Coupon coupon : expired) {
            coupon.setStatus(CouponStatus.EXPIRED);
            couponRepository.save(coupon);
            logger.info("优惠券已过期: id={}, code={}", coupon.getId(), coupon.getCode());
        }
    }

    // =========== 工具方法 ===========

    private String generateCouponCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("PX");
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        // 确保唯一
        while (couponRepository.findByCode(sb.toString()).isPresent()) {
            sb = new StringBuilder("PX");
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
        }
        return sb.toString();
    }

    // =========== 内部接口（供其他服务调用） ===========

    /**
     * 积分兑换：为用户创建专属优惠券并自动领取
     * 
     * @param userId     用户ID
     * @param couponName 优惠券名称（如"5元优惠券"）
     * @param amount     优惠金额
     * @param validDays  有效天数
     * @return 包含优惠券信息的Map
     */
    @Transactional
    public Map<String, Object> createCouponForUser(Long userId, String couponName, BigDecimal amount, int validDays) {
        // 1. 创建专属优惠券（数量为1，仅此用户可用）
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.builder()
                .code(generateCouponCode())
                .name(couponName)
                .description("积分兑换 - " + couponName)
                .type(CouponType.FIXED)
                .discountValue(amount)
                .minOrderAmount(amount) // 最低订单金额 = 券面值，防止倒贴
                .maxDiscountAmount(amount)
                .totalQuantity(1)
                .claimedQuantity(1) // 直接标记已领取
                .usedQuantity(0)
                .startTime(now)
                .endTime(now.plusDays(validDays))
                .status(CouponStatus.ACTIVE)
                .distributionType("CODE_ONLY") // 不在公开领取中心显示
                .build();
        couponRepository.save(coupon);

        // 2. 创建用户优惠券关联记录（自动领取）
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(coupon.getId())
                .status(UserCouponStatus.UNUSED)
                .build();
        userCouponRepository.save(userCoupon);

        logger.info("积分兑换创建优惠券成功: userId={}, couponId={}, name={}, amount={}",
                userId, coupon.getId(), couponName, amount);

        // 3. 返回优惠券信息
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("couponId", coupon.getId());
        result.put("couponCode", coupon.getCode());
        result.put("couponName", coupon.getName());
        result.put("amount", amount);
        result.put("validDays", validDays);
        result.put("endTime", coupon.getEndTime());
        return result;
    }

    private Map<String, Object> convertToMap(Coupon c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("code", c.getCode());
        map.put("name", c.getName());
        map.put("description", c.getDescription());
        map.put("type", c.getType().name());
        map.put("discountValue", c.getDiscountValue());
        map.put("minOrderAmount", c.getMinOrderAmount());
        map.put("maxDiscountAmount", c.getMaxDiscountAmount());
        map.put("totalQuantity", c.getTotalQuantity());
        map.put("claimedQuantity", c.getClaimedQuantity());
        map.put("usedQuantity", c.getUsedQuantity());
        map.put("startTime", c.getStartTime());
        map.put("endTime", c.getEndTime());
        map.put("status", c.getStatus().name());
        map.put("distributionType", c.getDistributionType());
        map.put("claimable", c.isClaimable());
        map.put("createdAt", c.getCreatedAt());
        return map;
    }
}
