package com.pixiv.commission.dto;

import com.pixiv.commission.entity.CommissionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private String clientAvatar;
    private Long artistId;
    private String artistName;
    private String artistAvatar;
    private Long conversationId;
    private String title;
    private String description;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private Boolean depositPaid;
    private Boolean finalPaid;
    private BigDecimal budget;
    private String referenceUrls;
    private String quoteNote;
    private Long commissionPlanId;
    private LocalDate deadline;
    private Integer revisionsAllowed;
    private Integer revisionsUsed;
    private String deliveryUrl;
    private String deliveryNote;
    private LocalDateTime deliveredAt;
    private String cancelReason;
    private CommissionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
