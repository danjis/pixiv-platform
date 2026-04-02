package com.pixiv.notification.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    private Long sessionId;
    private String message;
}
