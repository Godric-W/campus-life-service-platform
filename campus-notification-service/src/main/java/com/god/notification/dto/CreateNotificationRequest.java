package com.god.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateNotificationRequest {

    @NotNull(message = "接收人ID不能为空")
    private Long receiverId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过 100")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 500, message = "内容长度不能超过 500")
    private String content;

    @NotBlank(message = "通知类型不能为空")
    @Size(max = 32, message = "通知类型长度不能超过 32")
    private String type;

    private Long businessId;
    private String businessType;
}
