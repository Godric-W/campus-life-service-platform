package com.god.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateClubRequest {

    @NotBlank(message = "社团名称不能为空")
    @Size(max = 100, message = "社团名称长度不能超过 100")
    private String name;

    @Size(max = 1000, message = "社团介绍长度不能超过 1000")
    private String description;

    private String logo;
    private String contactInfo;
}
