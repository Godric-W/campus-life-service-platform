package com.god.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleDTO implements Serializable {

    private Long userId;
    private String studentNo;
    private String username;
    private String avatar;
    private String college;
    private String major;
    private String role;
    private Integer status;
}
