package com.god.userauth.controller;

import com.god.common.dto.UserSimpleDTO;
import com.god.common.result.Result;
import com.god.userauth.dto.UpdateUserRequest;
import com.god.userauth.service.UserService;
import com.god.userauth.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户资料查询与修改接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息", description = "查询当前登录用户的详细资料")
    @GetMapping("/me")
    public Result<UserProfileVO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @Operation(summary = "修改当前用户信息", description = "修改当前登录用户的资料")
    @PutMapping("/me")
    public Result<UserProfileVO> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
        return Result.success(userService.updateCurrentUser(request));
    }

    @Operation(summary = "查询用户资料", description = "根据用户ID查询用户详细资料")
    @GetMapping("/{id}")
    public Result<UserProfileVO> getUserProfile(
            @Parameter(description = "用户ID") @PathVariable("id") Long id) {
        return Result.success(userService.getUserProfile(id));
    }

    @Operation(summary = "查询用户简要信息", description = "根据用户ID查询用户简要信息，供其他服务调用")
    @GetMapping("/simple/{id}")
    public Result<UserSimpleDTO> getUserSimple(
            @Parameter(description = "用户ID") @PathVariable("id") Long id) {
        return Result.success(userService.getUserSimple(id));
    }

    @Operation(summary = "批量查询用户简要信息", description = "批量查询用户简要信息，供其他服务调用")
    @PostMapping("/simple/batch")
    public Result<Map<Long, UserSimpleDTO>> batchGetUserSimple(@RequestBody List<Long> userIds) {
        return Result.success(userService.batchGetUserSimple(userIds));
    }
}
