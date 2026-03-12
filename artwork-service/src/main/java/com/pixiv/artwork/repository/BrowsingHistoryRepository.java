package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.BrowsingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 浏览记录数据访问接口
 */
@Repository
public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {

    /**
     * 查询用户的浏览记录（按时间倒序）
     */
    Page<BrowsingHistory> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);

    /**
     * 查找用户对指定作品的浏览记录
     */
    Optional<BrowsingHistory> findByUserIdAndArtworkId(Long userId, Long artworkId);

    /**
     * 删除用户某条浏览记录
     */
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * 清空用户所有浏览记录
     */
    @Modifying
    @Query("DELETE FROM BrowsingHistory b WHERE b.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * 统计用户浏览记录数
     */
    long countByUserId(Long userId);
}
