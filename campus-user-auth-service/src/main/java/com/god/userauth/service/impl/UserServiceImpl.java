package com.god.userauth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.god.common.context.UserContext;
import com.god.common.dto.UserSimpleDTO;
import com.god.common.exception.BusinessException;
import com.god.common.result.ResultCode;
import com.god.userauth.dto.UpdateUserRequest;
import com.god.userauth.entity.User;
import com.god.userauth.mapper.UserMapper;
import com.god.userauth.service.UserService;
import com.god.userauth.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserProfileVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return getUserProfile(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateCurrentUser(UpdateUserRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User existing = getExistingUser(userId);
        validateUpdateUnique(userId, request);

        User user = new User();
        user.setId(existing.getId());
        if (StrUtil.isNotBlank(request.getUsername())) {
            user.setUsername(request.getUsername().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(normalizeBlank(request.getPhone()));
        }
        if (request.getEmail() != null) {
            user.setEmail(normalizeBlank(request.getEmail()));
        }
        if (request.getAvatar() != null) {
            user.setAvatar(normalizeBlank(request.getAvatar()));
        }
        if (request.getCollege() != null) {
            user.setCollege(normalizeBlank(request.getCollege()));
        }
        if (request.getMajor() != null) {
            user.setMajor(normalizeBlank(request.getMajor()));
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return getUserProfile(userId);
    }

    @Override
    public UserProfileVO getUserProfile(Long id) {
        return toProfile(getExistingUser(id));
    }

    @Override
    public UserSimpleDTO getUserSimple(Long id) {
        User user = getExistingUser(id);
        return UserSimpleDTO.builder()
                .userId(user.getId())
                .studentNo(user.getStudentNo())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .college(user.getCollege())
                .major(user.getMajor())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    @Override
    public Map<Long, UserSimpleDTO> batchGetUserSimple(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        
        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> UserSimpleDTO.builder()
                                .userId(user.getId())
                                .studentNo(user.getStudentNo())
                                .username(user.getUsername())
                                .avatar(user.getAvatar())
                                .college(user.getCollege())
                                .major(user.getMajor())
                                .role(user.getRole())
                                .status(user.getStatus())
                                .build()
                ));
    }

    private User getExistingUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void validateUpdateUnique(Long userId, UpdateUserRequest request) {
        if (StrUtil.isNotBlank(request.getUsername())) {
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, request.getUsername())
                    .ne(User::getId, userId));
            if (count > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "用户名已被使用");
            }
        }

        if (StrUtil.isNotBlank(request.getPhone())) {
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, request.getPhone())
                    .ne(User::getId, userId));
            if (count > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "手机号已被使用");
            }
        }

        if (StrUtil.isNotBlank(request.getEmail())) {
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, request.getEmail())
                    .ne(User::getId, userId));
            if (count > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "邮箱已被使用");
            }
        }
    }

    private UserProfileVO toProfile(User user) {
        return UserProfileVO.builder()
                .id(user.getId())
                .studentNo(user.getStudentNo())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .college(user.getCollege())
                .major(user.getMajor())
                .role(user.getRole())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    private String normalizeBlank(String value) {
        return StrUtil.isBlank(value) ? null : value.trim();
    }
}
