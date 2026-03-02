package com.pixiv.commission.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 创建/更新约稿方案请求 DTO
 */
@Data
public class CreateCommissionPlanRequest {

    @NotBlank(message = "方案标题不能为空")
    @Size(max = 200, message = "标题不能超过 200 字符")
    private String title;

    @Size(max = 5000, message = "描述不能超过 5000 字符")
    private String description;

    @NotNull(message = "起始价格不能为空")
    @DecimalMin(value = "1.00", message = "价格不能小于 1 元")
    private BigDecimal priceStart;

    @DecimalMin(value = "1.00", message = "价格不能小于 1 元")
    private BigDecimal priceEnd;

    @Min(value = 1, message = "工期至少 1 天")
    @Max(value = 365, message = "工期不能超过 365 天")
    private Integer estimatedDays;

    @Min(value = 0, message = "修改次数不能为负")
    @Max(value = 10, message = "修改次数不能超过 10")
    private Integer revisionsIncluded;

    @Size(max = 50, message = "分类不能超过 50 字符")
    private String category;

    private String sampleImages;

    private Integer sortOrder;
}
