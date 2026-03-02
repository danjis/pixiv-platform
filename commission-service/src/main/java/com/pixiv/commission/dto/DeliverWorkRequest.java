package com.pixiv.commission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliverWorkRequest {
    @NotBlank(message = "作品 URL 不能为空")
    @Size(max = 500)
    private String deliveryUrl;

    @Size(max = 2000, message = "交付说明不能超过 2000 字符")
    private String deliveryNote;
}
