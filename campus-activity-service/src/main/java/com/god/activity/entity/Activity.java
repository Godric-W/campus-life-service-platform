package com.god.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long clubId;
    private Long publisherId;
    private String title;
    private String description;
    private String location;
    private String coverImage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime signupDeadline;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
