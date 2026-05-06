package com.god.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("help_task")
public class HelpTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long publisherId;
    private Long accepterId;
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
