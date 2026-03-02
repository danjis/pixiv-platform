package com.pixiv.user.controller;

import com.pixiv.common.dto.Result;
import com.pixiv.user.dto.ArtistApplicationDTO;
import com.pixiv.user.dto.ArtistApplicationRequest;
import com.pixiv.user.service.ArtistApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 画师申请控制器
 * 处理用户端的画师申请相关请求
 */
@RestController
@RequestMapping("/api/users/artist-application")
@Tag(name = "画师申请", description = "画师申请相关接口")
public class ArtistApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ArtistApplicationController.class);

    private final ArtistApplicationService artistApplicationService;

    public ArtistApplicationController(ArtistApplicationService artistApplicationService) {
        this.artistApplicationService = artistApplicationService;
    }

    /**
     * 申请成为画师
     *
     * @param request 申请请求
     * @param authentication 认证信息
     * @return 画师申请 DTO
     */
    @PostMapping
    @Operation(summary = "申请成为画师", description = "用户提交画师申请")
    public ResponseEntity<Result<ArtistApplicationDTO>> applyForArtist(
            @Valid @RequestBody ArtistApplicationRequest request,
            Authentication authentication) {
        
        logger.info("收到画师申请请求: username={}", authentication.getName());

        // 从认证信息中获取用户 ID
        Long userId = Long.parseLong(authentication.getName());

        ArtistApplicationDTO result = artistApplicationService.applyForArtist(userId, request);

        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取当前用户的画师申请
     *
     * @param authentication 认证信息
     * @return 画师申请 DTO
     */
    @GetMapping
    @Operation(summary = "获取我的画师申请", description = "获取当前用户的画师申请状态")
    public ResponseEntity<Result<ArtistApplicationDTO>> getMyApplication(Authentication authentication) {
        
        logger.info("获取画师申请: username={}", authentication.getName());

        // 从认证信息中获取用户 ID
        Long userId = Long.parseLong(authentication.getName());

        ArtistApplicationDTO result = artistApplicationService.getApplicationByUserId(userId);

        return ResponseEntity.ok(Result.success(result));
    }
}
