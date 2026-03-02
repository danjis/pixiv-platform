package com.pixiv.user.repository;

import com.pixiv.user.entity.ActionType;
import com.pixiv.user.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志数据访问接口
 * 提供审计日志实体的 CRUD 操作和自定义查询方法
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * 根据管理员 ID 查找审计日志（分页）
     * @param adminId 管理员 ID
     * @param pageable 分页参数
     * @return 审计日志分页列表
     */
    Page<AuditLog> findByAdminId(Long adminId, Pageable pageable);
    
    /**
     * 根据操作类型查找审计日志（分页）
     * @param actionType 操作类型
     * @param pageable 分页参数
     * @return 审计日志分页列表
     */
    Page<AuditLog> findByActionType(ActionType actionType, Pageable pageable);
    
    /**
     * 根据目标类型和目标 ID 查找审计日志
     * @param targetType 目标类型
     * @param targetId 目标 ID
     * @param pageable 分页参数
     * @return 审计日志分页列表
     */
    Page<AuditLog> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);
    
    /**
     * 根据时间范围查找审计日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 审计日志分页列表
     */
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据管理员 ID 和操作类型查找审计日志
     * @param adminId 管理员 ID
     * @param actionType 操作类型
     * @param pageable 分页参数
     * @return 审计日志分页列表
     */
    Page<AuditLog> findByAdminIdAndActionType(Long adminId, ActionType actionType, Pageable pageable);
    
    /**
     * 查找最近的审计日志（按创建时间降序）
     * @param pageable 分页参数
     * @return 审计日志分页列表
     */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 统计指定管理员的操作次数
     * @param adminId 管理员 ID
     * @return 操作次数
     */
    long countByAdminId(Long adminId);
    
    /**
     * 统计指定操作类型的次数
     * @param actionType 操作类型
     * @return 操作次数
     */
    long countByActionType(ActionType actionType);
    
    /**
     * 查找指定时间范围内的审计日志列表
     * @param adminId 管理员 ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 审计日志列表
     */
    @Query("SELECT a FROM AuditLog a WHERE a.adminId = :adminId AND a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.createdAt DESC")
    List<AuditLog> findByAdminIdAndTimeRange(
        @Param("adminId") Long adminId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
