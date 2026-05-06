package com.god.userauth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.god.common.dto.LoginUserDTO;
import com.god.common.enums.UserRoleEnum;
import com.god.common.enums.UserStatusEnum;
import com.god.common.exception.BusinessException;
import com.god.common.result.ResultCode;
import com.god.common.utils.JwtUtil;
import com.god.common.utils.TokenUtil;
import com.god.userauth.config.JwtProperties;
import com.god.userauth.dto.LoginRequest;
import com.god.userauth.dto.RegisterRequest;
import com.god.userauth.entity.User;
import com.god.userauth.mapper.UserMapper;
import com.god.userauth.service.AuthService;
import com.god.userauth.vo.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        validateUnique(request);

        User user = new User();
        user.setStudentNo(request.getStudentNo());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(normalizeBlank(request.getPhone()));
        user.setEmail(normalizeBlank(request.getEmail()));
        user.setCollege(normalizeBlank(request.getCollege()));
        user.setMajor(normalizeBlank(request.getMajor()));
        user.setRole(UserRoleEnum.STUDENT.getCode());
        user.setStatus(UserStatusEnum.NORMAL.getCode());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = findByAccount(request.getAccount());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (!UserStatusEnum.NORMAL.getCode().equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        LoginUserDTO loginUser = toLoginUser(user);
        String token = JwtUtil.generateToken(buildClaims(loginUser), jwtProperties.getJwtSecret(), jwtProperties.getTokenExpireSeconds());
        return LoginResponse.builder()
                .token(TokenUtil.buildBearerToken(token))
                .expireSeconds(jwtProperties.getTokenExpireSeconds())
                .user(loginUser)
                .build();
    }

    private void validateUnique(RegisterRequest request) {
        Long studentNoCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStudentNo, request.getStudentNo()));
        if (studentNoCount > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "学号已被注册");
        }

        Long usernameCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (usernameCount > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已被使用");
        }

        if (StrUtil.isNotBlank(request.getPhone())) {
            Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, request.getPhone()));
            if (phoneCount > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "手机号已被使用");
            }
        }

        if (StrUtil.isNotBlank(request.getEmail())) {
            Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, request.getEmail()));
            if (emailCount > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "邮箱已被使用");
            }
        }
    }

    private User findByAccount(String account) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getStudentNo, account)
                .or()
                .eq(User::getUsername, account)
                .or()
                .eq(User::getPhone, account)
                .or()
                .eq(User::getEmail, account)
                .last("LIMIT 1"));
    }

    private LoginUserDTO toLoginUser(User user) {
        return LoginUserDTO.builder()
                .userId(user.getId())
                .studentNo(user.getStudentNo())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    private Map<String, Object> buildClaims(LoginUserDTO loginUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", loginUser.getUserId());
        claims.put("studentNo", loginUser.getStudentNo());
        claims.put("username", loginUser.getUsername());
        claims.put("avatar", loginUser.getAvatar());
        claims.put("role", loginUser.getRole());
        claims.put("status", loginUser.getStatus());
        return claims;
    }

    private String normalizeBlank(String value) {
        return StrUtil.isBlank(value) ? null : value.trim();
    }
}
