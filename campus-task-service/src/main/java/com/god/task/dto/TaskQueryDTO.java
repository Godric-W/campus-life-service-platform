package com.god.task.dto;

import com.god.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQueryDTO extends PageQueryDTO {

    private String keyword;
    private String taskType;
    private String status;
    private Long publisherId;
    private Long accepterId;
}
