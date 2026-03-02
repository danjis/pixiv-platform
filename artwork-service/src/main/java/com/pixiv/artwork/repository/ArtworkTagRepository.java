package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.ArtworkTag;
import com.pixiv.artwork.entity.TagSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 作品标签关联数据访问接口
 * 提供作品与标签关联关系的 CRUD 操作和自定义查询方法
 */
@Repository
public interface ArtworkTagRepository extends JpaRepository<ArtworkTag, Long> {
    
    /**
     * 根据作品 ID 查询所有标签关联
     */
    List<ArtworkTag> findByArtworkId(Long artworkId);
    
    /**
     * 批量根据作品 ID 列表查询所有标签关联（优化 N+1 查询）
     */
    List<ArtworkTag> findByArtworkIdIn(List<Long> artworkIds);
    
    /**
     * 根据标签 ID 查询所有作品关联
     */
    List<ArtworkTag> findByTagId(Long tagId);
    
    /**
     * 根据作品 ID 和标签 ID 查询关联
     */
    Optional<ArtworkTag> findByArtworkIdAndTagId(Long artworkId, Long tagId);
    
    /**
     * 检查作品和标签的关联是否存在
     */
    boolean existsByArtworkIdAndTagId(Long artworkId, Long tagId);
    
    /**
     * 根据作品 ID 和标签来源查询标签关联
     */
    List<ArtworkTag> findByArtworkIdAndSource(Long artworkId, TagSource source);
    
    /**
     * 根据作品 ID 删除所有标签关联
     */
    @Modifying
    @Query("DELETE FROM ArtworkTag at WHERE at.artworkId = :artworkId")
    void deleteByArtworkId(@Param("artworkId") Long artworkId);
    
    /**
     * 根据作品 ID 和标签来源删除标签关联
     */
    @Modifying
    @Query("DELETE FROM ArtworkTag at WHERE at.artworkId = :artworkId AND at.source = :source")
    void deleteByArtworkIdAndSource(@Param("artworkId") Long artworkId, @Param("source") TagSource source);
    
    /**
     * 根据标签 ID 删除所有关联
     */
    @Modifying
    @Query("DELETE FROM ArtworkTag at WHERE at.tagId = :tagId")
    void deleteByTagId(@Param("tagId") Long tagId);
    
    /**
     * 统计作品的标签数量
     */
    long countByArtworkId(Long artworkId);
    
    /**
     * 统计标签被使用的次数
     */
    long countByTagId(Long tagId);
    
    /**
     * 查询包含指定标签的所有作品 ID
     */
    @Query("SELECT at.artworkId FROM ArtworkTag at WHERE at.tagId = :tagId")
    List<Long> findArtworkIdsByTagId(@Param("tagId") Long tagId);
    
    /**
     * 查询包含指定标签列表的作品 ID（交集）
     */
    @Query("SELECT at.artworkId FROM ArtworkTag at WHERE at.tagId IN :tagIds GROUP BY at.artworkId HAVING COUNT(DISTINCT at.tagId) = :tagCount")
    List<Long> findArtworkIdsByTagIds(@Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount);
    
    /**
     * 查询作品的所有标签 ID
     */
    @Query("SELECT at.tagId FROM ArtworkTag at WHERE at.artworkId = :artworkId")
    List<Long> findTagIdsByArtworkId(@Param("artworkId") Long artworkId);
    
    /**
     * 根据置信度查询自动标签
     */
    @Query("SELECT at FROM ArtworkTag at WHERE at.artworkId = :artworkId AND at.source = 'AUTO' AND at.confidence >= :minConfidence")
    List<ArtworkTag> findAutoTagsByConfidence(@Param("artworkId") Long artworkId, @Param("minConfidence") Float minConfidence);
}
