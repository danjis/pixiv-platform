package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 评价数据访问接口
 * 提供评价记录的 CRUD 操作和自定义查询方法
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据作品 ID 查询所有评价（分页）
     */
    Page<Comment> findByArtworkId(Long artworkId, Pageable pageable);

    /**
     * 根据作品 ID 查询所有评价（按创建时间倒序）
     */
    Page<Comment> findByArtworkIdOrderByCreatedAtDesc(Long artworkId, Pageable pageable);

    /**
     * 查询作品的顶级评论（parentId 为 NULL），按创建时间倒序
     */
    @Query("SELECT c FROM Comment c WHERE c.artworkId = :artworkId AND c.parentId IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelComments(@Param("artworkId") Long artworkId, Pageable pageable);

    /**
     * 查询指定父评论的所有子回复，按创建时间正序
     */
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    /**
     * 批量查询多个父评论的所有子回复
     */
    @Query("SELECT c FROM Comment c WHERE c.parentId IN :parentIds ORDER BY c.createdAt ASC")
    List<Comment> findByParentIdIn(@Param("parentIds") List<Long> parentIds);

    /**
     * 根据作品 ID 查询所有评价（按创建时间正序）
     */
    Page<Comment> findByArtworkIdOrderByCreatedAtAsc(Long artworkId, Pageable pageable);

    /**
     * 根据用户 ID 查询所有评价（分页）
     */
    Page<Comment> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据作品 ID 和用户 ID 查询评价
     */
    List<Comment> findByArtworkIdAndUserId(Long artworkId, Long userId);

    /**
     * 统计作品的评价数量
     */
    long countByArtworkId(Long artworkId);

    /**
     * 统计用户的评价数量
     */
    long countByUserId(Long userId);

    /**
     * 根据作品 ID 删除所有评价
     */
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.artworkId = :artworkId")
    void deleteByArtworkId(@Param("artworkId") Long artworkId);

    /**
     * 根据用户 ID 删除所有评价
     */
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 查询指定时间范围内的评价
     */
    @Query("SELECT c FROM Comment c WHERE c.artworkId = :artworkId AND c.createdAt BETWEEN :startTime AND :endTime ORDER BY c.createdAt DESC")
    Page<Comment> findByArtworkIdAndCreatedAtBetween(@Param("artworkId") Long artworkId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 查询作品的最新评价
     */
    @Query("SELECT c FROM Comment c WHERE c.artworkId = :artworkId ORDER BY c.createdAt DESC")
    List<Comment> findLatestCommentsByArtworkId(@Param("artworkId") Long artworkId, Pageable pageable);

    /**
     * 根据 ID 和用户 ID 查询评价（用于验证评价所有权）
     */
    Optional<Comment> findByIdAndUserId(Long id, Long userId);

    /**
     * 查询用户在指定作品下的评价数量
     */
    long countByArtworkIdAndUserId(Long artworkId, Long userId);

    /**
     * 查询用户最近的评价
     */
    @Query("SELECT c FROM Comment c WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据内容关键词搜索评价
     */
    @Query("SELECT c FROM Comment c WHERE c.artworkId = :artworkId AND c.content LIKE %:keyword% ORDER BY c.createdAt DESC")
    Page<Comment> searchByKeyword(@Param("artworkId") Long artworkId, @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 全局搜索评论内容（管理端）
     */
    Page<Comment> findByContentContaining(String keyword, Pageable pageable);
}
