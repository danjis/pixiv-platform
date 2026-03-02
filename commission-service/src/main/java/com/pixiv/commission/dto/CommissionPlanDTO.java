package com.pixiv.commission.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionPlanDTO {
    private Long id;
    private Long artistId;
    private String artistName;
    private String artistAvatar;
    private String title;
    private String description;
    private BigDecimal priceStart;
    private BigDecimal priceEnd;
    private Integer estimatedDays;
    private Integer revisionsIncluded;
    private String category;
    private String sampleImages;
    private Boolean active;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
