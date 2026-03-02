package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.Favorite;
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
 * 收藏数据访问接口
 * 提供收藏记录的 CRUD 操作和自定义查询方法
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * 根据作品 ID 和用户 ID 查询收藏记录
     */
    Optional<Favorite> findByArtworkIdAndUserId(Long artworkId, Long userId);
    
    /**
     * 检查用户是否已收藏作品
     */
    boolean existsByArtworkIdAndUserId(Long artworkId, Long userId);
    
    /**
     * 根据作品 ID 查询所有收藏记录
     */
    List<Favorite> findByArtworkId(Long artworkId);
    
    /**
     * 根据用户 ID 查询所有收藏记录（分页）
     */
    Page<Favorite> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 统计作品的收藏数量
     */
    long countByArtworkId(Long artworkId);
    
    /**
     * 统计用户的收藏数量
     */
    long countByUserId(Long userId);
    
    /**
     * 根据作品 ID 和用户 ID 删除收藏记录
     */
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.artworkId = :artworkId AND f.userId = :userId")
    void deleteByArtworkIdAndUserId(@Param("artworkId") Long artworkId, @Param("userId") Long userId);
    
    /**
     * 根据作品 ID 删除所有收藏记录
     */
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.artworkId = :artworkId")
    void deleteByArtworkId(@Param("artworkId") Long artworkId);
    
    /**
     * 查询用户收藏的所有作品 ID
     */
    @Query("SELECT f.artworkId FROM Favorite f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Long> findArtworkIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户收藏的所有作品 ID（分页）
     */
    @Query("SELECT f.artworkId FROM Favorite f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    Page<Long> findArtworkIdsByUserIdPaged(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 批量检查用户是否收藏了指定作品列表
     */
    @Query("SELECT f.artworkId FROM Favorite f WHERE f.userId = :userId AND f.artworkId IN :artworkIds")
    List<Long> findFavoritedArtworkIds(@Param("userId") Long userId, @Param("artworkIds") List<Long> artworkIds);
    
    /**
     * 查询最近收藏的作品 ID（用于推荐）
     */
    @Query("SELECT f.artworkId FROM Favorite f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Long> findRecentFavoriteArtworkIds(@Param("userId") Long userId, Pageable pageable);
}
