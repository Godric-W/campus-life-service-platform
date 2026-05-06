package com.god.notification.dto;

import com.god.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationQueryDTO extends PageQueryDTO {

    private String type;
    private Integer readStatus;
}
