package com.god.market.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketItemListVO implements Serializable {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private String title;
    private BigDecimal price;
    private String category;
    private String coverImage;
    private String status;
    private Integer viewCount;
    private LocalDateTime createTime;
}
