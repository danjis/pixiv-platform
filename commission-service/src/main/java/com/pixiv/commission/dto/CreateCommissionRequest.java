package com.pixiv.commission.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建约稿请求 DTO
 *
 * 用户向画师发起约稿请求
 * 总金额和定金由画师在报价阶段设置
 */
@Data
public class CreateCommissionRequest {

    /** 目标画师 ID */
    @NotNull(message = "目标画师 ID 不能为空")
    private Long targetUserId;

    /** 关联的对话 ID（可选） */
    private Long conversationId;

    /** 约稿标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不能超过 200 字符")
    private String title;

    /** 需求描述 */
    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 5000, message = "描述长度需在 10-5000 字符之间")
    private String description;

    /** 用户预算（参考价格，画师报价可调整） */
    @DecimalMin(value = "1.00", message = "预算不能小于 1 元")
    @DecimalMax(value = "999999.99", message = "预算不能超过 999999.99 元")
    private BigDecimal budget;

    /** 期望截止日期 */
    @Future(message = "截止日期必须是未来日期")
    private LocalDate deadline;

    /** 参考图片 URL 列表（可选） */
    private String referenceUrls;

    /** 选择的约稿方案 ID（可选） */
    private Long commissionPlanId;
}
