package com.pixiv.user.repository;

import com.pixiv.user.entity.ArtistApplication;
import com.pixiv.user.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 画师申请数据访问接口
 * 提供画师申请实体的 CRUD 操作和自定义查询方法
 */
@Repository
public interface ArtistApplicationRepository extends JpaRepository<ArtistApplication, Long> {
    
    /**
     * 根据用户 ID 查找画师申请
     * @param userId 用户 ID
     * @return 画师申请对象（如果存在）
     */
    Optional<ArtistApplication> findByUserId(Long userId);
    
    /**
     * 根据用户 ID 和状态查找画师申请
     * @param userId 用户 ID
     * @param status 申请状态
     * @return 画师申请对象（如果存在）
     */
    Optional<ArtistApplication> findByUserIdAndStatus(Long userId, ApplicationStatus status);
    
    /**
     * 检查用户是否有待审核的申请
     * @param userId 用户 ID
     * @param status 申请状态
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByUserIdAndStatus(Long userId, ApplicationStatus status);
    
    /**
     * 根据状态查找画师申请列表（分页）
     * @param status 申请状态
     * @param pageable 分页参数
     * @return 画师申请分页列表
     */
    Page<ArtistApplication> findByStatus(ApplicationStatus status, Pageable pageable);
    
    /**
     * 根据审核人 ID 查找画师申请列表
     * @param reviewerId 审核人 ID
     * @param pageable 分页参数
     * @return 画师申请分页列表
     */
    Page<ArtistApplication> findByReviewerId(Long reviewerId, Pageable pageable);
    
    /**
     * 查找所有待审核的申请列表（按创建时间升序）
     * @param status 申请状态
     * @return 画师申请列表
     */
    List<ArtistApplication> findByStatusOrderByCreatedAtAsc(ApplicationStatus status);
    
    /**
     * 统计指定状态的申请数量
     * @param status 申请状态
     * @return 申请数量
     */
    long countByStatus(ApplicationStatus status);
}
