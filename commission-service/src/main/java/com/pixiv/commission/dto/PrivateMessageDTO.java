package com.pixiv.commission.dto;

import com.pixiv.commission.entity.MessageType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private MessageType messageType;
    private String attachmentUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
