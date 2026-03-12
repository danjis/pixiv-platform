package com.pixiv.user.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.user.dto.CheckinDTO;
import com.pixiv.user.dto.PointsDTO;
import com.pixiv.user.entity.PointRecord;
import com.pixiv.user.service.CheckinService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @GetMapping("/status")
    public ResponseEntity<Result<CheckinDTO>> getCheckinStatus(
            @RequestHeader("X-User-Id") Long userId) {
        CheckinDTO status = checkinService.getCheckinStatus(userId);
        return ResponseEntity.ok(Result.success(status));
    }

    @PostMapping
    public ResponseEntity<Result<CheckinDTO>> dailyCheckin(
            @RequestHeader("X-User-Id") Long userId) {
        CheckinDTO checkin = checkinService.doCheckin(userId);
        return ResponseEntity.ok(Result.success(checkin));
    }

    @GetMapping("/points")
    public ResponseEntity<Result<PointsDTO>> getPoints(
            @RequestHeader("X-User-Id") Long userId) {
        PointsDTO points = checkinService.getPoints(userId);
        return ResponseEntity.ok(Result.success(points));
    }

    @GetMapping("/points/records")
    public ResponseEntity<Result<Page<PointRecord>>> getPointRecords(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<PointRecord> records = checkinService.getPointRecords(userId, page, size);
        return ResponseEntity.ok(Result.success(records));
    }

    /**
     * 积分兑换优惠券
     */
    @PostMapping("/points/exchange")
    public ResponseEntity<Result<Map<String, Object>>> exchangePoints(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> body) {
        int itemId = Integer.parseInt(body.get("itemId").toString());
        Map<String, Object> result = checkinService.exchangePoints(userId, itemId);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取积分商城可兑换物品列表
     */
    @GetMapping("/points/shop")
    public ResponseEntity<Result<java.util.List<Map<String, Object>>>> getPointsShop() {
        return ResponseEntity.ok(Result.success(checkinService.getPointsShopItems()));
    }
}
