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
public class MarketItemVO implements Serializable {

    private Long id;
    private Long publisherId;
    private String publisherName;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String coverImage;
    private String images;
    private String contactInfo;
    private String status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
