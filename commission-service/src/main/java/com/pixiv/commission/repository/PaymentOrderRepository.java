package com.pixiv.commission.repository;

import com.pixiv.commission.entity.PaymentOrder;
import com.pixiv.commission.entity.PaymentStatus;
import com.pixiv.commission.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

        Optional<PaymentOrder> findByOrderNo(String orderNo);

        List<PaymentOrder> findByCommissionId(Long commissionId);

        List<PaymentOrder> findByCommissionIdAndPaymentType(Long commissionId, PaymentType paymentType);

        List<PaymentOrder> findByPayerId(Long payerId);

        org.springframework.data.domain.Page<PaymentOrder> findByPayerIdOrderByCreatedAtDesc(Long payerId,
                        org.springframework.data.domain.Pageable pageable);

        org.springframework.data.domain.Page<PaymentOrder> findByPayerIdAndStatusOrderByCreatedAtDesc(Long payerId,
                        PaymentStatus status, org.springframework.data.domain.Pageable pageable);

        org.springframework.data.domain.Page<PaymentOrder> findByPayerIdAndPaymentTypeOrderByCreatedAtDesc(Long payerId,
                        PaymentType paymentType, org.springframework.data.domain.Pageable pageable);

        Optional<PaymentOrder> findByCommissionIdAndPaymentTypeAndStatus(
                        Long commissionId, PaymentType paymentType, PaymentStatus status);

        boolean existsByCommissionIdAndPaymentTypeAndStatus(
                        Long commissionId, PaymentType paymentType, PaymentStatus status);

        @Query("SELECT o FROM PaymentOrder o WHERE o.status = 'PENDING' AND o.createdAt < :expireTime")
        List<PaymentOrder> findExpiredPendingOrders(@Param("expireTime") LocalDateTime expireTime);

        // ========== 财务统计查询 ==========

        /** 按状态统计总金额 */
        @Query("SELECT COALESCE(SUM(o.amount), 0) FROM PaymentOrder o WHERE o.status = :status")
        BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

        /** 按状态和类型统计总金额 */
        @Query("SELECT COALESCE(SUM(o.amount), 0) FROM PaymentOrder o WHERE o.status = :status AND o.paymentType = :type")
        BigDecimal sumAmountByStatusAndType(@Param("status") PaymentStatus status, @Param("type") PaymentType type);

        /** 按状态统计平台服务费总额 */
        @Query("SELECT COALESCE(SUM(o.platformFee), 0) FROM PaymentOrder o WHERE o.status = :status")
        BigDecimal sumPlatformFeeByStatus(@Param("status") PaymentStatus status);

        /** 按状态统计服务费折扣总额 */
        @Query("SELECT COALESCE(SUM(o.feeDiscount), 0) FROM PaymentOrder o WHERE o.status = :status")
        BigDecimal sumFeeDiscountByStatus(@Param("status") PaymentStatus status);

        /** 按时间范围和状态统计金额 */
        @Query("SELECT COALESCE(SUM(o.amount), 0) FROM PaymentOrder o WHERE o.status = :status AND COALESCE(o.paidAt, o.createdAt) >= :start AND COALESCE(o.paidAt, o.createdAt) < :end")
        BigDecimal sumAmountByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        /** 按时间范围、状态和类型统计金额 */
        @Query("SELECT COALESCE(SUM(o.amount), 0) FROM PaymentOrder o WHERE o.status = :status AND o.paymentType = :type AND COALESCE(o.paidAt, o.createdAt) >= :start AND COALESCE(o.paidAt, o.createdAt) < :end")
        BigDecimal sumAmountByStatusAndTypeAndPaidAtBetween(@Param("status") PaymentStatus status,
                        @Param("type") PaymentType type,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        /** 按时间范围统计平台服务费 */
        @Query("SELECT COALESCE(SUM(o.platformFee), 0) FROM PaymentOrder o WHERE o.status = :status AND COALESCE(o.paidAt, o.createdAt) >= :start AND COALESCE(o.paidAt, o.createdAt) < :end")
        BigDecimal sumPlatformFeeByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        /** 按状态统计订单数 */
        long countByStatus(PaymentStatus status);

        /** 按状态和类型统计订单数 */
        long countByStatusAndPaymentType(PaymentStatus status, PaymentType type);

        /** 按时间范围和状态统计订单数 */
        @Query("SELECT COUNT(o) FROM PaymentOrder o WHERE o.status = :status AND COALESCE(o.paidAt, o.createdAt) >= :start AND COALESCE(o.paidAt, o.createdAt) < :end")
        long countByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        /** 按 payeeId和状态查询（画师收入） */
        @Query("SELECT COALESCE(SUM(o.amount), 0) FROM PaymentOrder o WHERE o.payeeId = :payeeId AND o.status = :status AND o.paymentType != 'MEMBERSHIP'")
        BigDecimal sumAmountByPayeeIdAndStatus(@Param("payeeId") Long payeeId, @Param("status") PaymentStatus status);

        /** 画师总收入（基础约稿金额，不含服务费，不受优惠券影响） */
        @Query("SELECT COALESCE(SUM(CASE WHEN o.originalAmount IS NOT NULL THEN o.originalAmount ELSE o.amount - COALESCE(o.platformFee, 0) END), 0) FROM PaymentOrder o WHERE o.status = :status AND o.paymentType IN ('DEPOSIT', 'FINAL_PAYMENT')")
        BigDecimal sumArtistIncomeByStatus(@Param("status") PaymentStatus status);

        /** 优惠券补贴总额（平台承担的优惠券减免金额） */
        @Query("SELECT COALESCE(SUM(o.discountAmount), 0) FROM PaymentOrder o WHERE o.status = :status AND o.discountAmount IS NOT NULL")
        BigDecimal sumDiscountAmountByStatus(@Param("status") PaymentStatus status);

        /** 获取已支付的约稿订单（非会员），按时间排序 */
        @Query("SELECT o FROM PaymentOrder o WHERE o.status = 'PAID' AND o.paymentType != 'MEMBERSHIP' ORDER BY o.paidAt DESC")
        List<PaymentOrder> findPaidCommissionOrders(org.springframework.data.domain.Pageable pageable);

        /** 获取已退款的订单 */
        @Query("SELECT o FROM PaymentOrder o WHERE o.status = 'REFUNDED' ORDER BY o.refundedAt DESC")
        List<PaymentOrder> findRefundedOrders(org.springframework.data.domain.Pageable pageable);

        /** 获取所有已支付订单按时间范围 */
        @Query("SELECT o FROM PaymentOrder o WHERE o.status = :status AND o.paidAt >= :start AND o.paidAt < :end ORDER BY o.paidAt DESC")
        List<PaymentOrder> findByStatusAndPaidAtBetween(@Param("status") PaymentStatus status,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
