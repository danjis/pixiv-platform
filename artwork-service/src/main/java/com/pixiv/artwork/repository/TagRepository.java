package com.pixiv.artwork.repository;

import com.pixiv.artwork.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签数据访问接口
 * 提供标签的 CRUD 操作和自定义查询方法
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * 根据标签名称查询标签
     */
    Optional<Tag> findByName(String name);
    
    /**
     * 根据标签名称列表查询标签
     */
    List<Tag> findByNameIn(List<String> names);
    
    /**
     * 检查标签名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据使用次数查询热门标签（分页）
     */
    Page<Tag> findAllByOrderByUsageCountDesc(Pageable pageable);
    
    /**
     * 查询使用次数大于指定值的标签
     */
    @Query("SELECT t FROM Tag t WHERE t.usageCount > :minCount ORDER BY t.usageCount DESC")
    List<Tag> findPopularTags(@Param("minCount") Integer minCount, Pageable pageable);
    
    /**
     * 根据标签名称模糊搜索
     */
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:keyword% ORDER BY t.usageCount DESC")
    List<Tag> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 查询使用次数最高的前 N 个标签
     */
    List<Tag> findTop10ByOrderByUsageCountDesc();
    
    /**
     * 查询使用次数最高的前 N 个标签（自定义数量）
     */
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findTopNByUsageCount(Pageable pageable);
    
    /**
     * 统计标签总数
     */
    @Query("SELECT COUNT(t) FROM Tag t WHERE t.usageCount > 0")
    long countActiveTag();
}
