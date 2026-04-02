package com.pixiv.notification.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiChatSessionDTO {
    private Long id;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastMessage;
}
