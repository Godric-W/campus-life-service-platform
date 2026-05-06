package com.god.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
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
