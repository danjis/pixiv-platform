package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.user.dto.ArtistApplicationDTO;
import com.pixiv.user.dto.ArtistApplicationRequest;
import com.pixiv.user.entity.*;
import com.pixiv.user.repository.ArtistApplicationRepository;
import com.pixiv.user.repository.ArtistRepository;
import com.pixiv.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 画师申请服务
 * 处理画师申请相关的业务逻辑
 */
@Service
public class ArtistApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ArtistApplicationService.class);

    private final ArtistApplicationRepository applicationRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String NOTIFICATION_QUEUE = "notification.create";

    public ArtistApplicationService(ArtistApplicationRepository applicationRepository,
            ArtistRepository artistRepository,
            UserRepository userRepository,
            RabbitTemplate rabbitTemplate) {
        this.applicationRepository = applicationRepository;
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 申请成为画师
     *
     * @param userId  用户 ID
     * @param request 申请请求
     * @return 画师申请 DTO
     */
    @Transactional
    public ArtistApplicationDTO applyForArtist(Long userId, ArtistApplicationRequest request) {
        logger.info("用户申请成为画师: userId={}, portfolioUrl={}", userId, request.getPortfolioUrl());

        // 1. 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 检查用户是否已是画师
        if (artistRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_ARTIST, "您已经是画师，无需重复申请");
        }

        // 3. 检查是否有待审核的申请
        if (applicationRepository.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw new BusinessException(ErrorCode.ARTIST_APPLICATION_EXISTS, "您已有待审核的申请，请耐心等待");
        }

        // 4. 创建申请记录
        ArtistApplication application = new ArtistApplication();
        application.setUserId(userId);
        application.setPortfolioUrl(request.getPortfolioUrl());
        application.setDescription(request.getDescription());
        application.setSpecialties(toSpecialtiesJson(request.getSpecialties()));
        application.setContactInfo(request.getContactInfo());
        application.setStatus(ApplicationStatus.PENDING);

        application = applicationRepository.save(application);

        logger.info("画师申请创建成功: applicationId={}, userId={}", application.getId(), userId);

        // 5. 通知管理员审核
        sendNotification(null, "COMMISSION_REQUEST",
                "新的画师申请待审核，申请人 ID: " + userId,
                "/admin/artist-applications/" + application.getId());

        // 6. 返回申请信息
        return new ArtistApplicationDTO(application);
    }

    /**
     * 获取用户的画师申请
     *
     * @param userId 用户 ID
     * @return 画师申请 DTO（如果存在）
     */
    @Transactional(readOnly = true)
    public ArtistApplicationDTO getApplicationByUserId(Long userId) {
        logger.info("获取用户的画师申请: userId={}", userId);

        ArtistApplication application = applicationRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTIST_APPLICATION_NOT_FOUND, "未找到画师申请记录"));

        return new ArtistApplicationDTO(application);
    }

    /**
     * 根据 ID 获取画师申请
     *
     * @param applicationId 申请 ID
     * @return 画师申请 DTO
     */
    @Transactional(readOnly = true)
    public ArtistApplicationDTO getApplicationById(Long applicationId) {
        logger.info("获取画师申请详情: applicationId={}", applicationId);

        ArtistApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTIST_APPLICATION_NOT_FOUND, "未找到画师申请记录"));

        return new ArtistApplicationDTO(application);
    }

    /**
     * 获取待审核的申请列表
     *
     * @param pageable 分页参数
     * @return 画师申请分页列表
     */
    @Transactional(readOnly = true)
    public Page<ArtistApplicationDTO> getPendingApplications(Pageable pageable) {
        logger.info("获取待审核的申请列表: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<ArtistApplication> applications = applicationRepository.findByStatus(ApplicationStatus.PENDING, pageable);

        return enrichWithUsername(applications);
    }

    /**
     * 获取所有申请列表（按状态筛选）
     *
     * @param status   申请状态（可选）
     * @param pageable 分页参数
     * @return 画师申请分页列表
     */
    @Transactional(readOnly = true)
    public Page<ArtistApplicationDTO> getApplications(ApplicationStatus status, Pageable pageable) {
        logger.info("获取申请列表: status={}, page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        Page<ArtistApplication> applications;
        if (status != null) {
            applications = applicationRepository.findByStatus(status, pageable);
        } else {
            applications = applicationRepository.findAll(pageable);
        }

        return enrichWithUsername(applications);
    }

    /**
     * 将申请列表转换为 DTO 并填充用户名
     */
    private Page<ArtistApplicationDTO> enrichWithUsername(Page<ArtistApplication> applications) {
        // 批量查询用户名，避免 N+1 查询
        List<Long> userIds = applications.getContent().stream()
                .map(ArtistApplication::getUserId)
                .distinct()
                .toList();
        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return applications.map(app -> {
            ArtistApplicationDTO dto = new ArtistApplicationDTO(app);
            dto.setUsername(usernameMap.get(app.getUserId()));
            return dto;
        });
    }

    /**
     * 审核画师申请
     *
     * @param applicationId 申请 ID
     * @param reviewerId    审核人 ID
     * @param approved      是否批准
     * @param reviewComment 审核意见
     * @return 更新后的画师申请 DTO
     */
    @Transactional
    public ArtistApplicationDTO reviewApplication(Long applicationId, Long reviewerId,
            Boolean approved, String reviewComment) {
        logger.info("审核画师申请: applicationId={}, reviewerId={}, approved={}",
                applicationId, reviewerId, approved);

        // 1. 查找申请记录
        ArtistApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTIST_APPLICATION_NOT_FOUND, "未找到画师申请记录"));

        // 2. 检查申请状态
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该申请已被审核，无法重复审核");
        }

        // 3. 更新申请状态
        application.setStatus(approved ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        application.setReviewerId(reviewerId);
        application.setReviewComment(reviewComment);
        application.setReviewedAt(LocalDateTime.now());

        application = applicationRepository.save(application);

        // 4. 如果批准，创建画师记录并升级用户角色
        if (approved) {
            createArtistProfile(application);
        }

        logger.info("画师申请审核完成: applicationId={}, status={}", applicationId, application.getStatus());

        // 5. 通知用户审核结果
        String notificationType = approved ? "APPLICATION_APPROVED" : "APPLICATION_REJECTED";
        String content = approved
                ? "恭喜！您的画师申请已通过审核，您现在可以发布作品了"
                : "您的画师申请未通过审核" + (reviewComment != null ? "，原因：" + reviewComment : "");
        sendNotification(application.getUserId(), notificationType, content, "/user/artist-application");

        // 6. 返回更新后的申请信息
        return new ArtistApplicationDTO(application);
    }

    /**
     * 创建画师档案
     *
     * @param application 画师申请
     */
    private void createArtistProfile(ArtistApplication application) {
        logger.info("创建画师档案: userId={}", application.getUserId());

        // 1. 查找用户
        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 检查是否已是画师（防止重复创建）
        if (artistRepository.existsByUserId(user.getId())) {
            logger.warn("用户已是画师，跳过创建: userId={}", user.getId());
            return;
        }

        // 3. 创建画师记录
        Artist artist = new Artist();
        artist.setUser(user);
        artist.setPortfolioUrl(application.getPortfolioUrl());
        artist.setDescription(application.getDescription());
        artist.setSpecialties(application.getSpecialties());
        artist.setContactInfo(application.getContactInfo());
        artist.setFollowerCount(0);
        artist.setArtworkCount(0);
        artist.setCommissionCount(0);
        artist.setAcceptingCommissions(true);

        artistRepository.save(artist);

        // 4. 升级用户角色为画师
        user.setRole(UserRole.ARTIST);
        userRepository.save(user);

        logger.info("画师档案创建成功: userId={}, artistId={}", user.getId(), artist.getId());
    }

    /**
     * 统计待审核申请数量
     *
     * @return 待审核申请数量
     */
    @Transactional(readOnly = true)
    public long countPendingApplications() {
        return applicationRepository.countByStatus(ApplicationStatus.PENDING);
    }

    private String toSpecialtiesJson(java.util.List<String> specialties) {
        java.util.List<String> sanitized = specialties == null
                ? java.util.Collections.emptyList()
                : specialties.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .limit(10)
                        .toList();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(sanitized);
        } catch (Exception e) {
            logger.warn("序列化画师申请 specialties 失败", e);
            return "[]";
        }
    }

    /**
     * 发送通知消息到 RabbitMQ
     * 
     * @param userId  接收通知的用户 ID（null 表示管理员通知，暂不发送）
     * @param type    通知类型
     * @param content 通知内容
     * @param linkUrl 跳转链接
     */
    private void sendNotification(Long userId, String type, String content, String linkUrl) {
        if (userId == null) {
            // 管理员通知：当前通知系统是基于 userId 的，管理员通知待后续扩展
            logger.info("管理员通知（待扩展）: type={}, content={}", type, content);
            return;
        }
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("type", type);
            message.put("content", content);
            message.put("linkUrl", linkUrl);
            rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            logger.warn("发送通知失败: userId={}, type={}, error={}", userId, type, e.getMessage());
        }
    }
}
