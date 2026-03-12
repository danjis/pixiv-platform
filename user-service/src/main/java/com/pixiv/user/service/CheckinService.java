package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.dto.Result;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.user.feign.CommissionServiceClient;
import com.pixiv.user.dto.CheckinDTO;
import com.pixiv.user.dto.PointsDTO;
import com.pixiv.user.entity.PointRecord;
import com.pixiv.user.entity.UserCheckin;
import com.pixiv.user.entity.UserPoints;
import com.pixiv.user.entity.MembershipLevel;
import com.pixiv.user.repository.PointRecordRepository;
import com.pixiv.user.repository.UserCheckinRepository;
import com.pixiv.user.repository.UserPointsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckinService {

    private static final Logger logger = LoggerFactory.getLogger(CheckinService.class);

    private final UserCheckinRepository userCheckinRepository;
    private final UserPointsRepository userPointsRepository;
    private final PointRecordRepository pointRecordRepository;
    private final MembershipService membershipService;
    private final CommissionServiceClient commissionServiceClient;

    public CheckinService(UserCheckinRepository userCheckinRepository,
            UserPointsRepository userPointsRepository,
            PointRecordRepository pointRecordRepository,
            MembershipService membershipService,
            CommissionServiceClient commissionServiceClient) {
        this.userCheckinRepository = userCheckinRepository;
        this.userPointsRepository = userPointsRepository;
        this.pointRecordRepository = pointRecordRepository;
        this.membershipService = membershipService;
        this.commissionServiceClient = commissionServiceClient;
    }

    public CheckinDTO getCheckinStatus(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        LocalDate today = LocalDate.now();
        boolean checkedInToday = userCheckinRepository.existsByUserIdAndCheckinDate(userId, today);

        // 获取最近的签到记录以计算连续天数
        int consecutiveDays = 0;
        int lastPointsEarned = 0;
        List<UserCheckin> recentCheckins = userCheckinRepository.findByUserIdOrderByCheckinDateDesc(userId);
        if (!recentCheckins.isEmpty()) {
            UserCheckin latest = recentCheckins.get(0);
            if (latest.getCheckinDate().equals(today) || latest.getCheckinDate().equals(today.minusDays(1))) {
                consecutiveDays = latest.getConsecutiveDays();
            }
            if (latest.getCheckinDate().equals(today)) {
                lastPointsEarned = latest.getPointsEarned();
            }
        }

        UserPoints userPoints = getOrCreateUserPoints(userId);
        MembershipLevel level = membershipService.getEffectiveLevel(userId);
        int multiplier = membershipService.getPointsMultiplier(level);
        List<Boolean> weekCheckins = getWeekCheckins(userId);

        CheckinDTO dto = new CheckinDTO();
        dto.setCheckedInToday(checkedInToday);
        dto.setConsecutiveDays(consecutiveDays);
        dto.setPointsEarned(lastPointsEarned);
        dto.setTotalPoints(userPoints.getTotalPoints());
        dto.setAvailablePoints(userPoints.getAvailablePoints());
        dto.setMembershipLevel(level.name());
        dto.setMultiplier(multiplier);
        dto.setWeekCheckins(weekCheckins);

        return dto;
    }

    @Transactional
    public CheckinDTO doCheckin(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        LocalDate today = LocalDate.now();

        // 检查今日是否已签到
        if (userCheckinRepository.existsByUserIdAndCheckinDate(userId, today)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "今日已签到，请勿重复签到");
        }

        // 计算连续签到天数
        int consecutiveDays = 1;
        LocalDate yesterday = today.minusDays(1);
        UserCheckin yesterdayCheckin = userCheckinRepository.findByUserIdAndCheckinDate(userId, yesterday).orElse(null);
        if (yesterdayCheckin != null) {
            consecutiveDays = yesterdayCheckin.getConsecutiveDays() + 1;
        }

        // 计算积分
        int basePoints = getBasePoints(consecutiveDays);
        MembershipLevel level = membershipService.getEffectiveLevel(userId);
        int multiplier = membershipService.getPointsMultiplier(level);
        int earnedPoints = basePoints * multiplier;

        // 保存签到记录
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setPointsEarned(earnedPoints);
        checkin.setConsecutiveDays(consecutiveDays);
        checkin.setCreatedAt(LocalDateTime.now());
        userCheckinRepository.save(checkin);
        logger.info("用户 {} 签到成功，连续 {} 天，获得 {} 积分（基础 {} * 倍率 {}）",
                userId, consecutiveDays, earnedPoints, basePoints, multiplier);

        // 更新用户积分
        UserPoints userPoints = getOrCreateUserPoints(userId);
        userPoints.setTotalPoints(userPoints.getTotalPoints() + earnedPoints);
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() + earnedPoints);
        userPoints.setUpdatedAt(LocalDateTime.now());
        userPointsRepository.save(userPoints);

        // 保存积分记录
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(earnedPoints);
        record.setType("CHECKIN");
        record.setDescription("签到奖励（连续第" + consecutiveDays + "天）");
        record.setCreatedAt(LocalDateTime.now());
        pointRecordRepository.save(record);

        // 构建返回DTO
        List<Boolean> weekCheckins = getWeekCheckins(userId);

        CheckinDTO dto = new CheckinDTO();
        dto.setCheckedInToday(true);
        dto.setConsecutiveDays(consecutiveDays);
        dto.setPointsEarned(earnedPoints);
        dto.setTotalPoints(userPoints.getTotalPoints());
        dto.setAvailablePoints(userPoints.getAvailablePoints());
        dto.setMembershipLevel(level.name());
        dto.setMultiplier(multiplier);
        dto.setWeekCheckins(weekCheckins);

        return dto;
    }

    public PointsDTO getPoints(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        UserPoints userPoints = getOrCreateUserPoints(userId);
        MembershipLevel level = membershipService.getEffectiveLevel(userId);

        PointsDTO dto = new PointsDTO();
        dto.setUserId(userId);
        dto.setTotalPoints(userPoints.getTotalPoints());
        dto.setAvailablePoints(userPoints.getAvailablePoints());
        dto.setMembershipLevel(level.name());

        return dto;
    }

    public Page<PointRecord> getPointRecords(Long userId, int page, int size) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (page < 1) {
            page = 1;
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "每页数量必须在1-100之间");
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        return pointRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    private int getBasePoints(int consecutiveDays) {
        // 7天一个周期: 10, 20, 30, 40, 50, 60, 100
        int dayInCycle = ((consecutiveDays - 1) % 7) + 1;
        switch (dayInCycle) {
            case 1:
                return 10;
            case 2:
                return 20;
            case 3:
                return 30;
            case 4:
                return 40;
            case 5:
                return 50;
            case 6:
                return 60;
            case 7:
                return 100;
            default:
                return 10;
        }
    }

    private UserPoints getOrCreateUserPoints(Long userId) {
        UserPoints userPoints = userPointsRepository.findByUserId(userId).orElse(null);
        if (userPoints == null) {
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(0);
            userPoints.setAvailablePoints(0);
            userPoints.setCreatedAt(LocalDateTime.now());
            userPoints.setUpdatedAt(LocalDateTime.now());
            userPoints = userPointsRepository.save(userPoints);
            logger.info("为用户 {} 创建积分记录", userId);
        }
        return userPoints;
    }

    private List<Boolean> getWeekCheckins(Long userId) {
        LocalDate today = LocalDate.now();
        // 计算本周一和本周日
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = today.with(DayOfWeek.SUNDAY);

        List<UserCheckin> weekRecords = userCheckinRepository
                .findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(userId, monday, sunday);

        // 构建周一到周日的签到状态数组
        List<Boolean> checkins = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            boolean checkedIn = false;
            for (UserCheckin record : weekRecords) {
                if (record.getCheckinDate().equals(date)) {
                    checkedIn = true;
                    break;
                }
            }
            checkins.add(checkedIn);
        }

        return checkins;
    }

    // =========== 积分商城 ===========

    /** 积分商城物品定义 */
    private static final List<Map<String, Object>> SHOP_ITEMS = new ArrayList<>();
    static {
        Map<String, Object> item1 = new LinkedHashMap<>();
        item1.put("id", 1);
        item1.put("name", "5元约稿优惠券");
        item1.put("description", "可用于约稿订单抵扣5元");
        item1.put("pointsCost", 100);
        item1.put("couponAmount", 5);
        item1.put("icon", "🎫");
        SHOP_ITEMS.add(item1);

        Map<String, Object> item2 = new LinkedHashMap<>();
        item2.put("id", 2);
        item2.put("name", "10元约稿优惠券");
        item2.put("description", "可用于约稿订单抵扣10元");
        item2.put("pointsCost", 180);
        item2.put("couponAmount", 10);
        item2.put("icon", "🎟️");
        SHOP_ITEMS.add(item2);

        Map<String, Object> item3 = new LinkedHashMap<>();
        item3.put("id", 3);
        item3.put("name", "25元约稿优惠券");
        item3.put("description", "可用于约稿订单抵扣25元");
        item3.put("pointsCost", 400);
        item3.put("couponAmount", 25);
        item3.put("icon", "💎");
        SHOP_ITEMS.add(item3);

        Map<String, Object> item4 = new LinkedHashMap<>();
        item4.put("id", 4);
        item4.put("name", "50元约稿优惠券");
        item4.put("description", "可用于约稿订单抵扣50元");
        item4.put("pointsCost", 700);
        item4.put("couponAmount", 50);
        item4.put("icon", "👑");
        SHOP_ITEMS.add(item4);
    }

    public List<Map<String, Object>> getPointsShopItems() {
        return SHOP_ITEMS;
    }

    @Transactional
    public Map<String, Object> exchangePoints(Long userId, int itemId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        // 查找商品
        Map<String, Object> shopItem = SHOP_ITEMS.stream()
                .filter(item -> (int) item.get("id") == itemId)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "兑换物品不存在"));

        int pointsCost = (int) shopItem.get("pointsCost");
        String itemName = (String) shopItem.get("name");

        // 检查积分是否足够
        UserPoints userPoints = getOrCreateUserPoints(userId);
        if (userPoints.getAvailablePoints() < pointsCost) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    "积分不足，需要 " + pointsCost + " 积分，当前可用 " + userPoints.getAvailablePoints() + " 积分");
        }

        // 扣除积分
        userPoints.setAvailablePoints(userPoints.getAvailablePoints() - pointsCost);
        userPoints.setUpdatedAt(LocalDateTime.now());
        userPointsRepository.save(userPoints);

        // 记录积分消费
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(-pointsCost);
        record.setType("EXCHANGE");
        record.setDescription("积分兑换: " + itemName);
        record.setCreatedAt(LocalDateTime.now());
        pointRecordRepository.save(record);

        logger.info("用户 {} 兑换 {}，消耗 {} 积分", userId, itemName, pointsCost);

        // 调用 commission-service 创建真实优惠券
        int couponAmount = (int) shopItem.get("couponAmount");
        try {
            Map<String, Object> couponRequest = new LinkedHashMap<>();
            couponRequest.put("userId", userId);
            couponRequest.put("couponName", itemName);
            couponRequest.put("amount", couponAmount);
            couponRequest.put("validDays", 30);
            Result<Map<String, Object>> couponResult = commissionServiceClient.createCouponForUser(couponRequest);
            if (couponResult != null && couponResult.getCode() == 200) {
                logger.info("优惠券创建成功: userId={}, coupon={}", userId, couponResult.getData());
            } else {
                logger.warn("优惠券创建返回异常: userId={}, result={}", userId, couponResult);
            }
        } catch (Exception e) {
            logger.error("调用 commission-service 创建优惠券失败: userId={}, itemName={}", userId, itemName, e);
            // 不抛出异常，积分已扣除，记录日志后续人工补发
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("itemName", itemName);
        result.put("pointsCost", pointsCost);
        result.put("remainingPoints", userPoints.getAvailablePoints());
        result.put("message", "兑换成功！" + itemName + " 已发放到您的优惠券账户");
        return result;
    }
}
