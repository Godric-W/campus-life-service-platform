package com.god.task.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpTaskVO implements Serializable {

    private Long id;
    private Long publisherId;
    private String publisherName;
    private Long accepterId;
    private String accepterName;
    private String title;
    private String description;
    private String taskType;
    private BigDecimal reward;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime deadline;
    private String contactInfo;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
