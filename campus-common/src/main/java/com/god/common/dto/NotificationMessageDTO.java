package com.god.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDTO implements Serializable {

    private Long receiverId;
    private String title;
    private String content;
    private String type;
    private Long businessId;
    private String businessType;
}
