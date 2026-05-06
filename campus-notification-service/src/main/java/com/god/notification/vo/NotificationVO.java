package com.god.notification.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO implements Serializable {

    private Long id;
    private Long receiverId;
    private String title;
    private String content;
    private String type;
    private Long businessId;
    private String businessType;
    private Integer readStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
