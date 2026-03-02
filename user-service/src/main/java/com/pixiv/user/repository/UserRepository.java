package com.pixiv.user.repository;

import com.pixiv.user.entity.User;
import com.pixiv.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 提供用户实体的 CRUD 操作和自定义查询方法
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户对象（如果存在）
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱地址
     * @return 用户对象（如果存在）
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱地址
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByEmail(String email);

    /**
     * 根据角色查找用户列表
     * 
     * @param role 用户角色
     * @return 用户列表
     */
    java.util.List<User> findByRole(UserRole role);

    /**
     * 根据用户名或邮箱查找用户（用于登录）
     * 
     * @param username 用户名
     * @param email    邮箱地址
     * @return 用户对象（如果存在）
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    // ---- Admin 管理用 ----
    org.springframework.data.domain.Page<User> findByRole(UserRole role,
            org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, org.springframework.data.domain.Pageable pageable);
}
