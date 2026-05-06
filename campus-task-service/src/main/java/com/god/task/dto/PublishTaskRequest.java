package com.god.task.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PublishTaskRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100, message = "标题长度不能超过 100")
    private String title;

    @Size(max = 1000, message = "描述长度不能超过 1000")
    private String description;

    @NotBlank(message = "任务类型不能为空")
    @Size(max = 50, message = "任务类型长度不能超过 50")
    private String taskType;

    @NotNull(message = "悬赏金额不能为空")
    @DecimalMin(value = "0.00", message = "悬赏金额不能为负数")
    private BigDecimal reward;

    @Size(max = 255, message = "取货地址长度不能超过 255")
    private String pickupAddress;

    @Size(max = 255, message = "送达地址长度不能超过 255")
    private String deliveryAddress;

    private LocalDateTime deadline;
    private String contactInfo;
}
