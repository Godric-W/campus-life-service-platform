package com.god.userauth.vo;

import com.god.common.dto.LoginUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {

    private String token;
    private Long expireSeconds;
    private LoginUserDTO user;
}
