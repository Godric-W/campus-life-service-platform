package com.god.notification.controller;

import com.god.common.result.PageResult;
import com.god.common.result.Result;
import com.god.notification.dto.CreateNotificationRequest;
import com.god.notification.dto.NotificationQueryDTO;
import com.god.notification.service.NotificationService;
import com.god.notification.vo.NotificationVO;
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

@Tag(name = "消息通知管理", description = "消息通知查询、已读、删除等接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "创建通知", description = "创建一条新通知（供其他服务调用）")
    @PostMapping
    public Result<Long> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        return Result.success(notificationService.createNotification(request));
    }

    @Operation(summary = "我的通知", description = "分页查询当前用户的通知列表")
    @GetMapping
    public Result<PageResult<NotificationVO>> getMyNotifications(NotificationQueryDTO queryDTO) {
        return Result.success(notificationService.getMyNotifications(queryDTO));
    }

    @Operation(summary = "未读数量", description = "查询当前用户的未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }

    @Operation(summary = "标记已读", description = "标记单条通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "全部已读", description = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success();
    }

    @Operation(summary = "删除通知", description = "删除一条通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable("id") Long id) {
        notificationService.deleteNotification(id);
        return Result.success();
    }
}
