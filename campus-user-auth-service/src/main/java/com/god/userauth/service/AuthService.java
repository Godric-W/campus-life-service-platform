package com.god.userauth.service;

import com.god.userauth.dto.LoginRequest;
import com.god.userauth.dto.RegisterRequest;
import com.god.userauth.vo.LoginResponse;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
