package com.pixiv.user.repository;

import com.pixiv.user.entity.Artist;
import com.pixiv.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 画师数据访问接口
 * 提供画师实体的 CRUD 操作和自定义查询方法
 */
@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    
    /**
     * 根据用户 ID 查找画师信息
     * @param userId 用户 ID
     * @return 画师对象（如果存在）
     */
    Optional<Artist> findByUserId(Long userId);
    
    /**
     * 根据用户对象查找画师信息
     * @param user 用户对象
     * @return 画师对象（如果存在）
     */
    Optional<Artist> findByUser(User user);
    
    /**
     * 检查用户是否已是画师
     * @param userId 用户 ID
     * @return 如果是画师返回 true，否则返回 false
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 查找接受约稿的画师列表（分页）
     * @param acceptingCommissions 是否接受约稿
     * @param pageable 分页参数
     * @return 画师分页列表
     */
    Page<Artist> findByAcceptingCommissions(Boolean acceptingCommissions, Pageable pageable);
    
    /**
     * 根据粉丝数量排序查找画师列表（分页）
     * @param pageable 分页参数
     * @return 画师分页列表
     */
    Page<Artist> findAllByOrderByFollowerCountDesc(Pageable pageable);
    
    /**
     * 根据作品数量排序查找画师列表（分页）
     * @param pageable 分页参数
     * @return 画师分页列表
     */
    Page<Artist> findAllByOrderByArtworkCountDesc(Pageable pageable);
    
    /**
     * 查找粉丝数量大于指定值的画师列表
     * @param minFollowers 最小粉丝数
     * @param pageable 分页参数
     * @return 画师分页列表
     */
    Page<Artist> findByFollowerCountGreaterThanEqual(Integer minFollowers, Pageable pageable);
}
