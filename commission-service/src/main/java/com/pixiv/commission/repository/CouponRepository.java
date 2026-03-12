package com.pixiv.commission.repository;

import com.pixiv.commission.entity.Coupon;
import com.pixiv.commission.entity.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 优惠券数据访问接口
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /** 根据优惠券代码查找 */
    Optional<Coupon> findByCode(String code);

    /** 查找有效的优惠券（前台展示） */
    Page<Coupon> findByStatusAndEndTimeAfterOrderByCreatedAtDesc(
            CouponStatus status, LocalDateTime now, Pageable pageable);

    /** 查找所有优惠券（管理端） */
    Page<Coupon> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 查找已过期但状态未更新的优惠券 */
    List<Coupon> findByStatusAndEndTimeBefore(CouponStatus status, LocalDateTime now);
}
