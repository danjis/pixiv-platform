package com.pixiv.commission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateConversationRequest {
    @NotNull(message = "对方用户 ID 不能为空")
    private Long targetUserId;
}
