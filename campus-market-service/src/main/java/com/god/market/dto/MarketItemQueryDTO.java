package com.god.market.dto;

import com.god.common.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MarketItemQueryDTO extends PageQueryDTO {

    private String keyword;
    private String category;
    private String status;
    private Long sellerId;
}
