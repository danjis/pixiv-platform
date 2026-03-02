package com.pixiv.commission.repository;

import com.pixiv.commission.entity.Commission;
import com.pixiv.commission.entity.CommissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 约稿 Repository
 * 
 * 提供约稿数据的持久化操作
 * 
 * @author Pixiv Platform Team
 */
@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {

    /**
     * 根据客户 ID 查询约稿列表（分页）
     * 
     * @param clientId 客户 ID
     * @param pageable 分页参数
     * @return 约稿列表
     */
    Page<Commission> findByClientId(Long clientId, Pageable pageable);

    /**
     * 根据画师 ID 查询约稿列表（分页）
     * 
     * @param artistId 画师 ID
     * @param pageable 分页参数
     * @return 约稿列表
     */
    Page<Commission> findByArtistId(Long artistId, Pageable pageable);

    /**
     * 根据客户 ID 和状态查询约稿列表
     * 
     * @param clientId 客户 ID
     * @param status   约稿状态
     * @param pageable 分页参数
     * @return 约稿列表
     */
    Page<Commission> findByClientIdAndStatus(Long clientId, CommissionStatus status, Pageable pageable);

    /**
     * 根据画师 ID 和状态查询约稿列表
     * 
     * @param artistId 画师 ID
     * @param status   约稿状态
     * @param pageable 分页参数
     * @return 约稿列表
     */
    Page<Commission> findByArtistIdAndStatus(Long artistId, CommissionStatus status, Pageable pageable);

    /**
     * 统计客户的约稿数量
     * 
     * @param clientId 客户 ID
     * @return 约稿数量
     */
    long countByClientId(Long clientId);

    /**
     * 统计画师的约稿数量
     * 
     * @param artistId 画师 ID
     * @return 约稿数量
     */
    long countByArtistId(Long artistId);

    /**
     * 根据对话 ID 查询关联的约稿列表
     */
    List<Commission> findByConversationId(Long conversationId);
}
