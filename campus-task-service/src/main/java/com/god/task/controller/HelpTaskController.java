package com.god.task.controller;

import com.god.common.result.PageResult;
import com.god.common.result.Result;
import com.god.task.dto.PublishTaskRequest;
import com.god.task.dto.TaskQueryDTO;
import com.god.task.service.HelpTaskService;
import com.god.task.vo.HelpTaskListVO;
import com.god.task.vo.HelpTaskVO;
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

@Tag(name = "跑腿任务管理", description = "校园跑腿互助任务发布、接单、完成等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class HelpTaskController {

    private final HelpTaskService helpTaskService;

    @Operation(summary = "发布任务", description = "发布一个新的跑腿互助任务")
    @PostMapping
    public Result<Long> publishTask(@Valid @RequestBody PublishTaskRequest request) {
        return Result.success(helpTaskService.publishTask(request));
    }

    @Operation(summary = "任务大厅", description = "分页查询任务列表，支持关键词、类型、状态筛选")
    @GetMapping
    public Result<PageResult<HelpTaskListVO>> getTaskList(TaskQueryDTO queryDTO) {
        return Result.success(helpTaskService.getTaskList(queryDTO));
    }

    @Operation(summary = "任务详情", description = "查看任务详细信息")
    @GetMapping("/{id}")
    public Result<HelpTaskVO> getTaskDetail(
            @Parameter(description = "任务ID") @PathVariable("id") Long id) {
        return Result.success(helpTaskService.getTaskDetail(id));
    }

    @Operation(summary = "接单", description = "接受一个任务")
    @PutMapping("/{id}/accept")
    public Result<Void> acceptTask(
            @Parameter(description = "任务ID") @PathVariable("id") Long id) {
        helpTaskService.acceptTask(id);
        return Result.success();
    }

    @Operation(summary = "完成任务", description = "标记任务为已完成，仅接单者可操作")
    @PutMapping("/{id}/complete")
    public Result<Void> completeTask(
            @Parameter(description = "任务ID") @PathVariable("id") Long id) {
        helpTaskService.completeTask(id);
        return Result.success();
    }

    @Operation(summary = "取消任务", description = "取消未接单的任务，仅发布者可操作")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelTask(
            @Parameter(description = "任务ID") @PathVariable("id") Long id) {
        helpTaskService.cancelTask(id);
        return Result.success();
    }

    @Operation(summary = "我发布的任务", description = "查询当前用户发布的所有任务")
    @GetMapping("/my-published")
    public Result<PageResult<HelpTaskListVO>> getMyPublished(TaskQueryDTO queryDTO) {
        return Result.success(helpTaskService.getMyPublished(queryDTO));
    }

    @Operation(summary = "我接的任务", description = "查询当前用户接单的所有任务")
    @GetMapping("/my-accepted")
    public Result<PageResult<HelpTaskListVO>> getMyAccepted(TaskQueryDTO queryDTO) {
        return Result.success(helpTaskService.getMyAccepted(queryDTO));
    }
}
