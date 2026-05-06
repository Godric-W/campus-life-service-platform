package com.god.activity.controller;

import com.god.activity.dto.CreateClubRequest;
import com.god.activity.service.ClubService;
import com.god.activity.vo.ClubVO;
import com.god.common.result.Result;
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

@Tag(name = "社团管理", description = "校园社团创建、查询、修改等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService clubService;

    @Operation(summary = "创建社团", description = "创建一个新的校园社团")
    @PostMapping
    public Result<Long> createClub(@Valid @RequestBody CreateClubRequest request) {
        return Result.success(clubService.createClub(request));
    }

    @Operation(summary = "社团列表", description = "查询所有社团列表")
    @GetMapping
    public Result<List<ClubVO>> getClubList() {
        return Result.success(clubService.getClubList());
    }

    @Operation(summary = "社团详情", description = "查看社团详细信息")
    @GetMapping("/{id}")
    public Result<ClubVO> getClubDetail(
            @Parameter(description = "社团ID") @PathVariable("id") Long id) {
        return Result.success(clubService.getClubDetail(id));
    }

    @Operation(summary = "修改社团", description = "修改社团信息，仅社团管理员可操作")
    @PutMapping("/{id}")
    public Result<Void> updateClub(
            @Parameter(description = "社团ID") @PathVariable("id") Long id,
            @Valid @RequestBody CreateClubRequest request) {
        clubService.updateClub(id, request);
        return Result.success();
    }

    @Operation(summary = "我管理的社团", description = "查询当前用户管理的所有社团")
    @GetMapping("/my")
    public Result<List<ClubVO>> getMyClubs() {
        return Result.success(clubService.getMyClubs());
    }
}
