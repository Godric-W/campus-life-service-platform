package com.god.activity.dto;

import com.god.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityQueryDTO extends PageQueryDTO {

    private String keyword;
    private Long clubId;
    private String status;
    private Long publisherId;
}
