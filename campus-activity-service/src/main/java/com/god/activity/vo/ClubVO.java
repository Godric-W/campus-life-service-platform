package com.god.activity.vo;

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
public class ClubVO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private String logo;
    private Long adminId;
    private String adminName;
    private String contactInfo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
