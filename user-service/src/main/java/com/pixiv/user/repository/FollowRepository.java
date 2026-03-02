package com.pixiv.user.repository;

import com.pixiv.user.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 关注关系数据访问接口
 * 提供关注关系实体的 CRUD 操作和自定义查询方法
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    /**
     * 根据关注者和被关注者查找关注关系
     * @param followerId 关注者 ID
     * @param followingId 被关注者 ID
     * @return 关注关系对象（如果存在）
     */
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    /**
     * 检查是否存在关注关系
     * @param followerId 关注者 ID
     * @param followingId 被关注者 ID
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    /**
     * 删除关注关系
     * @param followerId 关注者 ID
     * @param followingId 被关注者 ID
     */
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    /**
     * 查找用户关注的所有人（分页）
     * @param followerId 关注者 ID
     * @param pageable 分页参数
     * @return 关注关系分页列表
     */
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);
    
    /**
     * 查找用户的所有粉丝（分页）
     * @param followingId 被关注者 ID
     * @param pageable 分页参数
     * @return 关注关系分页列表
     */
    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);
    
    /**
     * 统计用户关注的人数
     * @param followerId 关注者 ID
     * @return 关注人数
     */
    long countByFollowerId(Long followerId);
    
    /**
     * 统计用户的粉丝数
     * @param followingId 被关注者 ID
     * @return 粉丝数
     */
    long countByFollowingId(Long followingId);
    
    /**
     * 查找用户关注的所有人的 ID 列表
     * @param followerId 关注者 ID
     * @return 被关注者 ID 列表
     */
    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :followerId")
    List<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);
    
    /**
     * 查找用户的所有粉丝的 ID 列表
     * @param followingId 被关注者 ID
     * @return 关注者 ID 列表
     */
    @Query("SELECT f.followerId FROM Follow f WHERE f.followingId = :followingId")
    List<Long> findFollowerIdsByFollowingId(@Param("followingId") Long followingId);
}
