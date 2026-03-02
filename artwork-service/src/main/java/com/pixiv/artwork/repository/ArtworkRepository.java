package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.Artwork;
import com.pixiv.artwork.entity.ArtworkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 作品数据访问接口
 * 提供作品的 CRUD 操作和自定义查询方法
 */
@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long>, JpaSpecificationExecutor<Artwork> {

    /**
     * 根据画师 ID 查询作品列表（分页）
     */
    Page<Artwork> findByArtistIdAndStatus(Long artistId, ArtworkStatus status, Pageable pageable);

    /**
     * 根据画师 ID 查询所有作品（分页）
     */
    Page<Artwork> findByArtistId(Long artistId, Pageable pageable);

    /**
     * 根据状态查询作品列表（分页）
     */
    Page<Artwork> findByStatus(ArtworkStatus status, Pageable pageable);

    /**
     * 根据标题或描述模糊搜索作品（分页）
     */
    @Query("SELECT a FROM Artwork a WHERE a.status = :status AND (a.title LIKE %:keyword% OR a.description LIKE %:keyword%)")
    Page<Artwork> searchByKeyword(@Param("keyword") String keyword, @Param("status") ArtworkStatus status,
            Pageable pageable);

    /**
     * 根据热度分数查询作品（分页，用于热门推荐）
     */
    Page<Artwork> findByStatusOrderByHotnessScoreDesc(ArtworkStatus status, Pageable pageable);

    /**
     * 根据创建时间查询作品（分页，用于最新作品）
     */
    Page<Artwork> findByStatusOrderByCreatedAtDesc(ArtworkStatus status, Pageable pageable);

    /**
     * 根据点赞数查询作品（分页）
     */
    Page<Artwork> findByStatusOrderByLikeCountDesc(ArtworkStatus status, Pageable pageable);

    /**
     * 根据收藏数查询作品（分页）
     */
    Page<Artwork> findByStatusOrderByFavoriteCountDesc(ArtworkStatus status, Pageable pageable);

    /**
     * 根据浏览数查询作品（分页）
     */
    Page<Artwork> findByStatusOrderByViewCountDesc(ArtworkStatus status, Pageable pageable);

    /**
     * 统计画师的作品数量
     */
    long countByArtistIdAndStatus(Long artistId, ArtworkStatus status);

    /**
     * 查询指定时间范围内的作品
     */
    @Query("SELECT a FROM Artwork a WHERE a.status = :status AND a.createdAt BETWEEN :startTime AND :endTime")
    Page<Artwork> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") ArtworkStatus status,
            Pageable pageable);

    /**
     * 查询关注画师的作品（用于动态流）
     */
    @Query("SELECT a FROM Artwork a WHERE a.artistId IN :artistIds AND a.status = :status ORDER BY a.createdAt DESC")
    Page<Artwork> findByArtistIdInAndStatus(@Param("artistIds") List<Long> artistIds,
            @Param("status") ArtworkStatus status,
            Pageable pageable);

    /**
     * 根据 ID 和状态查询作品
     */
    Optional<Artwork> findByIdAndStatus(Long id, ArtworkStatus status);

    /**
     * 查询热度分数大于指定值的作品
     */
    @Query("SELECT a FROM Artwork a WHERE a.status = :status AND a.hotnessScore > :minScore ORDER BY a.hotnessScore DESC")
    Page<Artwork> findByHotnessScoreGreaterThan(@Param("minScore") Double minScore,
            @Param("status") ArtworkStatus status,
            Pageable pageable);

    /**
     * 根据作品 ID 列表和状态查询作品（分页）
     */
    Page<Artwork> findByIdInAndStatus(List<Long> ids, ArtworkStatus status, Pageable pageable);

    /**
     * 根据作品 ID 列表、状态和关键词查询作品（分页）
     */
    @Query("SELECT a FROM Artwork a WHERE a.id IN :ids AND a.status = :status AND (a.title LIKE %:keyword% OR a.description LIKE %:keyword%)")
    Page<Artwork> findByIdInAndStatusAndTitleOrDescriptionContaining(
            @Param("ids") List<Long> ids,
            @Param("status") ArtworkStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 搜索建议：返回匹配标题（去重，按浏览量降序）
     */
    @Query("SELECT DISTINCT a.title FROM Artwork a WHERE a.status = :status AND LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.viewCount DESC")
    List<String> findTitleSuggestions(@Param("keyword") String keyword, @Param("status") ArtworkStatus status,
            Pageable pageable);
}
