package com.pixiv.user.service;

import com.pixiv.common.constant.ErrorCode;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.user.dto.ArtistApplicationDTO;
import com.pixiv.user.entity.ActionType;
import com.pixiv.user.entity.Admin;
import com.pixiv.user.entity.AuditLog;
import com.pixiv.user.repository.AdminRepository;
import com.pixiv.user.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员服务
 * 处理管理员相关的业务逻辑
 */
@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final AdminRepository adminRepository;
    private final AuditLogRepository auditLogRepository;
    private final ArtistApplicationService artistApplicationService;

    public AdminService(AdminRepository adminRepository,
                       AuditLogRepository auditLogRepository,
                       ArtistApplicationService artistApplicationService) {
        this.adminRepository = adminRepository;
        this.auditLogRepository = auditLogRepository;
        this.artistApplicationService = artistApplicationService;
    }

    /**
     * 审核画师申请
     *
     * @param adminId 管理员 ID
     * @param applicationId 申请 ID
     * @param approved 是否批准
     * @param reviewComment 审核意见
     * @return 更新后的画师申请 DTO
     */
    @Transactional
    public ArtistApplicationDTO reviewApplication(Long adminId, Long applicationId, 
                                                  Boolean approved, String reviewComment) {
        logger.info("管理员审核画师申请: adminId={}, applicationId={}, approved={}", 
                   adminId, applicationId, approved);

        // 1. 验证管理员存在
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND, "管理员不存在"));

        // 2. 调用画师申请服务进行审核
        ArtistApplicationDTO result = artistApplicationService.reviewApplication(
            applicationId, adminId, approved, reviewComment
        );

        // 3. 记录审计日志
        createAuditLog(
            adminId,
            approved ? ActionType.APPROVE_ARTIST_APPLICATION : ActionType.REJECT_ARTIST_APPLICATION,
            String.format("审核画师申请 ID: %d, 结果: %s, 意见: %s", 
                         applicationId, approved ? "批准" : "拒绝", reviewComment),
            "ArtistApplication",
            applicationId
        );

        logger.info("管理员审核完成: adminId={}, applicationId={}, status={}", 
                   adminId, applicationId, result.getStatus());

        return result;
    }

    /**
     * 创建审计日志
     *
     * @param adminId 管理员 ID
     * @param actionType 操作类型
     * @param description 操作描述
     * @param targetType 目标类型
     * @param targetId 目标 ID
     */
    private void createAuditLog(Long adminId, ActionType actionType, String description,
                               String targetType, Long targetId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAdminId(adminId);
        auditLog.setActionType(actionType);
        auditLog.setDescription(description);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);

        auditLogRepository.save(auditLog);

        logger.debug("审计日志已创建: adminId={}, actionType={}, targetType={}, targetId={}", 
                    adminId, actionType, targetType, targetId);
    }

    /**
     * 验证管理员身份
     *
     * @param adminId 管理员 ID
     * @return 如果是管理员返回 true，否则返回 false
     */
    @Transactional(readOnly = true)
    public boolean isAdmin(Long adminId) {
        return adminRepository.existsById(adminId);
    }

    /**
     * 根据用户名查找管理员
     *
     * @param username 用户名
     * @return 管理员对象
     */
    @Transactional(readOnly = true)
    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND, "管理员不存在"));
    }
}
