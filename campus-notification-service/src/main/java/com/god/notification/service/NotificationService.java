package com.god.notification.service;

import com.god.common.result.PageResult;
import com.god.notification.dto.CreateNotificationRequest;
import com.god.notification.dto.NotificationQueryDTO;
import com.god.notification.vo.NotificationVO;

public interface NotificationService {

    Long createNotification(CreateNotificationRequest request);

    PageResult<NotificationVO> getMyNotifications(NotificationQueryDTO queryDTO);

    Long getUnreadCount();

    void markAsRead(Long id);

    void markAllAsRead();

    void deleteNotification(Long id);
}
