package com.pixiv.commission.repository;

import com.pixiv.commission.entity.UserCoupon;
import com.pixiv.commission.entity.UserCouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户优惠券数据访问接口
 */
@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    /** 查找用户某张优惠券 */
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    /** 查找用户的所有优惠券（分页） */
    Page<UserCoupon> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 查找用户特定状态的优惠券 */
    Page<UserCoupon> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, UserCouponStatus status, Pageable pageable);

    /** 查找用户可用于指定订单金额的优惠券 */
    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon c " +
            "WHERE uc.userId = :userId AND uc.status = 'UNUSED' " +
            "AND c.status = 'ACTIVE' AND c.endTime > CURRENT_TIMESTAMP " +
            "AND c.minOrderAmount <= :orderAmount " +
            "ORDER BY c.discountValue DESC")
    List<UserCoupon> findAvailableForOrder(@Param("userId") Long userId,
            @Param("orderAmount") java.math.BigDecimal orderAmount);

    /** 统计用户优惠券数量 */
    long countByUserIdAndStatus(Long userId, UserCouponStatus status);
}
