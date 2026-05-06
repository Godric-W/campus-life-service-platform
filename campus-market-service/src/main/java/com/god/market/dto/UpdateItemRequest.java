package com.god.market.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateItemRequest {

    @Size(max = 100, message = "标题长度不能超过 100")
    private String title;

    @Size(max = 1000, message = "描述长度不能超过 1000")
    private String description;

    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    @Size(max = 50, message = "分类长度不能超过 50")
    private String category;

    private String coverImage;
    private String images;
    private String contactInfo;
}
