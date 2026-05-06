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
public class ActivityVO implements Serializable {

    private Long id;
    private Long clubId;
    private String clubName;
    private Long publisherId;
    private String publisherName;
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
    private Boolean isRegistered;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
