package com.pixiv.commission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendPrivateMessageRequest {
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过 2000 字符")
    private String content;

    /** 消息类型：TEXT / IMAGE */
    private String messageType;

    /** 附件 URL（图片消息时使用） */
    @Size(max = 500)
    private String attachmentUrl;
}
