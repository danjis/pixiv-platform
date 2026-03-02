package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 点赞数据访问接口
 * 提供点赞记录的 CRUD 操作和自定义查询方法
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    /**
     * 根据作品 ID 和用户 ID 查询点赞记录
     */
    Optional<Like> findByArtworkIdAndUserId(Long artworkId, Long userId);
    
    /**
     * 检查用户是否已点赞作品
     */
    boolean existsByArtworkIdAndUserId(Long artworkId, Long userId);
    
    /**
     * 根据作品 ID 查询所有点赞记录
     */
    List<Like> findByArtworkId(Long artworkId);
    
    /**
     * 根据用户 ID 查询所有点赞记录（分页）
     */
    Page<Like> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 统计作品的点赞数量
     */
    long countByArtworkId(Long artworkId);
    
    /**
     * 统计用户的点赞数量
     */
    long countByUserId(Long userId);
    
    /**
     * 根据作品 ID 和用户 ID 删除点赞记录
     */
    @Modifying
    @Query("DELETE FROM Like l WHERE l.artworkId = :artworkId AND l.userId = :userId")
    void deleteByArtworkIdAndUserId(@Param("artworkId") Long artworkId, @Param("userId") Long userId);
    
    /**
     * 根据作品 ID 删除所有点赞记录
     */
    @Modifying
    @Query("DELETE FROM Like l WHERE l.artworkId = :artworkId")
    void deleteByArtworkId(@Param("artworkId") Long artworkId);
    
    /**
     * 查询用户点赞的所有作品 ID
     */
    @Query("SELECT l.artworkId FROM Like l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    List<Long> findArtworkIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户点赞的所有作品 ID（分页）
     */
    @Query("SELECT l.artworkId FROM Like l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    Page<Long> findArtworkIdsByUserIdPaged(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 批量检查用户是否点赞了指定作品列表
     */
    @Query("SELECT l.artworkId FROM Like l WHERE l.userId = :userId AND l.artworkId IN :artworkIds")
    List<Long> findLikedArtworkIds(@Param("userId") Long userId, @Param("artworkIds") List<Long> artworkIds);
}
