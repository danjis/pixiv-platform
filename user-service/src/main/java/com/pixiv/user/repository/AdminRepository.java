package com.pixiv.user.repository;

import com.pixiv.user.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 管理员数据访问接口
 * 提供管理员实体的 CRUD 操作和自定义查询方法
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * 根据用户名查找管理员
     * @param username 用户名
     * @return 管理员对象（如果存在）
     */
    Optional<Admin> findByUsername(String username);
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByUsername(String username);
}
