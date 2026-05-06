package com.god.activity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublishActivityRequest {

    private Long clubId;

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 100, message = "标题长度不能超过 100")
    private String title;

    @Size(max = 1000, message = "描述长度不能超过 1000")
    private String description;

    @Size(max = 255, message = "地点长度不能超过 255")
    private String location;

    private String coverImage;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private LocalDateTime signupDeadline;

    @Min(value = 1, message = "最大参与人数至少为 1")
    private Integer maxParticipants;
}
