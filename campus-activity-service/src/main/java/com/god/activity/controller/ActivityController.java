package com.god.activity.controller;

import com.god.activity.dto.ActivityQueryDTO;
import com.god.activity.dto.PublishActivityRequest;
import com.god.activity.service.ActivityService;
import com.god.activity.vo.ActivityListVO;
import com.god.activity.vo.ActivityVO;
import com.god.common.result.PageResult;
import com.god.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "活动管理", description = "校园活动发布、报名、取消等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "发布活动", description = "发布一个新的校园活动")
    @PostMapping
    public Result<Long> publishActivity(@Valid @RequestBody PublishActivityRequest request) {
        return Result.success(activityService.publishActivity(request));
    }

    @Operation(summary = "活动列表", description = "分页查询活动列表，支持关键词、社团、状态筛选")
    @GetMapping
    public Result<PageResult<ActivityListVO>> getActivityList(ActivityQueryDTO queryDTO) {
        return Result.success(activityService.getActivityList(queryDTO));
    }

    @Operation(summary = "活动详情", description = "查看活动详细信息")
    @GetMapping("/{id}")
    public Result<ActivityVO> getActivityDetail(
            @Parameter(description = "活动ID") @PathVariable("id") Long id) {
        return Result.success(activityService.getActivityDetail(id));
    }

    @Operation(summary = "报名活动", description = "报名参加活动")
    @PostMapping("/{id}/register")
    public Result<Void> registerActivity(
            @Parameter(description = "活动ID") @PathVariable("id") Long id) {
        activityService.registerActivity(id);
        return Result.success();
    }

    @Operation(summary = "取消报名", description = "取消活动报名")
    @DeleteMapping("/{id}/register")
    public Result<Void> cancelRegistration(
            @Parameter(description = "活动ID") @PathVariable("id") Long id) {
        activityService.cancelRegistration(id);
        return Result.success();
    }

    @Operation(summary = "取消活动", description = "取消活动，仅发布者可操作")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelActivity(
            @Parameter(description = "活动ID") @PathVariable("id") Long id) {
        activityService.cancelActivity(id);
        return Result.success();
    }

    @Operation(summary = "我的报名", description = "查询当前用户报名的所有活动")
    @GetMapping("/my-registered")
    public Result<PageResult<ActivityListVO>> getMyRegistered(ActivityQueryDTO queryDTO) {
        return Result.success(activityService.getMyRegistered(queryDTO));
    }

    @Operation(summary = "我发布的活动", description = "查询当前用户发布的所有活动")
    @GetMapping("/my-published")
    public Result<PageResult<ActivityListVO>> getMyPublished(ActivityQueryDTO queryDTO) {
        return Result.success(activityService.getMyPublished(queryDTO));
    }
}
