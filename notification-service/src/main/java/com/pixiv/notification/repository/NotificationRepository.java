package com.pixiv.notification.repository;

import com.pixiv.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 通知数据访问层
 * 
 * 提供通知的数据库操作接口
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 根据用户 ID 分页查询通知列表
     * 
     * @param userId 用户 ID
     * @param pageable 分页参数
     * @return 通知分页结果
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 统计用户的未读通知数量
     * 
     * @param userId 用户 ID
     * @return 未读通知数量
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    /**
     * 标记用户的所有通知为已读
     * 
     * @param userId 用户 ID
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
