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
public class HelpTaskListVO implements Serializable {

    private Long id;
    private Long publisherId;
    private String publisherName;
    private String title;
    private String taskType;
    private BigDecimal reward;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime deadline;
    private String status;
    private LocalDateTime createTime;
}
