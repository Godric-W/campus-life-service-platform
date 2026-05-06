package com.god.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("market_item")
public class MarketItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;
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
