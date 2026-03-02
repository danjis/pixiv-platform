package com.pixiv.commission.repository;

import com.pixiv.commission.entity.PaymentOrder;
import com.pixiv.commission.entity.PaymentStatus;
import com.pixiv.commission.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

        Optional<PaymentOrder> findByOrderNo(String orderNo);

        List<PaymentOrder> findByCommissionId(Long commissionId);

        List<PaymentOrder> findByCommissionIdAndPaymentType(Long commissionId, PaymentType paymentType);

        List<PaymentOrder> findByPayerId(Long payerId);

        Optional<PaymentOrder> findByCommissionIdAndPaymentTypeAndStatus(
                        Long commissionId, PaymentType paymentType, PaymentStatus status);

        boolean existsByCommissionIdAndPaymentTypeAndStatus(
                        Long commissionId, PaymentType paymentType, PaymentStatus status);

        @org.springframework.data.jpa.repository.Query("SELECT o FROM PaymentOrder o WHERE o.status = 'PENDING' AND o.createdAt < :expireTime")
        List<PaymentOrder> findExpiredPendingOrders(
                        @org.springframework.data.repository.query.Param("expireTime") java.time.LocalDateTime expireTime);
}
