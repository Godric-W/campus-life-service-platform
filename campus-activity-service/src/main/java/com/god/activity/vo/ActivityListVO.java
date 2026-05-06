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
public class ActivityListVO implements Serializable {

    private Long id;
    private Long clubId;
    private String clubName;
    private String title;
    private String location;
    private String coverImage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime signupDeadline;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status;
    private LocalDateTime createTime;
}
