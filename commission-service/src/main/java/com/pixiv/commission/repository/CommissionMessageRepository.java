package com.pixiv.commission.repository;

import com.pixiv.commission.entity.CommissionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 约稿消息 Repository
 * 
 * 提供约稿消息数据的持久化操作
 * 
 * @author Pixiv Platform Team
 */
@Repository
public interface CommissionMessageRepository extends JpaRepository<CommissionMessage, Long> {

    /**
     * 根据约稿 ID 查询消息列表（按创建时间升序）
     * 
     * @param commissionId 约稿 ID
     * @return 消息列表
     */
    List<CommissionMessage> findByCommissionIdOrderByCreatedAtAsc(Long commissionId);

    /**
     * 统计约稿的消息数量
     * 
     * @param commissionId 约稿 ID
     * @return 消息数量
     */
    long countByCommissionId(Long commissionId);

    /**
     * 根据约稿 ID 删除所有消息
     *
     * @param commissionId 约稿 ID
     */
    void deleteByCommissionId(Long commissionId);
}
