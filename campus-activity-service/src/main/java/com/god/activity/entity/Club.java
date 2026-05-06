package com.god.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("club")
public class Club {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String logo;
    private Long adminId;
    private String contactInfo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
