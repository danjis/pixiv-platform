package com.pixiv.commission.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 画师报价请求 DTO
 *
 * 画师收到约稿请求后，设定价格、定金、工期等
 */
@Data
public class QuoteCommissionRequest {

    /** 总金额（画师报价） */
    @NotNull(message = "总金额不能为空")
    @DecimalMin(value = "1.00", message = "金额不能小于 1 元")
    @DecimalMax(value = "999999.99", message = "金额不能超过 999999.99 元")
    private BigDecimal totalAmount;

    /** 定金比例（0.0~1.0），默认 0.3 即 30% */
    @DecimalMin(value = "0.1", message = "定金比例不能小于 10%")
    @DecimalMax(value = "1.0", message = "定金比例不能大于 100%")
    private BigDecimal depositRatio;

    /** 截止日期（画师确认的交付日期） */
    @Future(message = "截止日期必须是未来日期")
    private LocalDate deadline;

    /** 允许修改次数 */
    @Min(value = 0, message = "修改次数不能为负数")
    @Max(value = 10, message = "修改次数不能超过 10 次")
    private Integer revisionsAllowed;

    /** 报价说明（可选） */
    @Size(max = 2000, message = "报价说明不能超过 2000 字符")
    private String quoteNote;
}
