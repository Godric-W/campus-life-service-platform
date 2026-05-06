package com.god.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.god.common.context.UserContext;
import com.god.common.exception.BusinessException;
import com.god.common.result.PageResult;
import com.god.common.result.ResultCode;
import com.god.notification.dto.CreateNotificationRequest;
import com.god.notification.dto.NotificationQueryDTO;
import com.god.notification.entity.Notification;
import com.god.notification.mapper.NotificationMapper;
import com.god.notification.service.NotificationService;
import com.god.notification.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setReceiverId(request.getReceiverId());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setType(request.getType());
        notification.setBusinessId(request.getBusinessId());
        notification.setBusinessType(request.getBusinessType());
        notification.setReadStatus(0);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        return notification.getId();
    }

    @Override
    public PageResult<NotificationVO> getMyNotifications(NotificationQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Page<Notification> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, userId);

        if (queryDTO.getType() != null) {
            wrapper.eq(Notification::getType, queryDTO.getType());
        }
        if (queryDTO.getReadStatus() != null) {
            wrapper.eq(Notification::getReadStatus, queryDTO.getReadStatus());
        }

        wrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> result = notificationMapper.selectPage(page, wrapper);

        List<NotificationVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), (long) queryDTO.getPageNum(), (long) queryDTO.getPageSize(), voList);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getReadStatus, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOTIFICATION_NOT_FOUND);
        }

        if (!notification.getReceiverId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能操作自己的通知");
        }

        notification.setReadStatus(1);
        notification.setUpdateTime(LocalDateTime.now());
        notificationMapper.updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getReadStatus, 0)
                .set(Notification::getReadStatus, 1)
                .set(Notification::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOTIFICATION_NOT_FOUND);
        }

        if (!notification.getReceiverId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能删除自己的通知");
        }

        notificationMapper.deleteById(id);
    }

    private NotificationVO toVO(Notification notification) {
        return NotificationVO.builder()
                .id(notification.getId())
                .receiverId(notification.getReceiverId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .businessId(notification.getBusinessId())
                .businessType(notification.getBusinessType())
                .readStatus(notification.getReadStatus())
                .createTime(notification.getCreateTime())
                .updateTime(notification.getUpdateTime())
                .build();
    }
}
