import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Notification } from '@/types'
import { notificationApi } from '@/api'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const notifications = ref<Notification[]>([])

  async function fetchNotifications(pageNum = 1, pageSize = 20) {
    const response = await notificationApi.getNotifications(pageNum, pageSize)
    notifications.value = response.data.records
    return response
  }

  async function fetchUnreadCount() {
    const response = await notificationApi.getUnreadCount()
    unreadCount.value = response.data
    return response
  }

  async function markAsRead(id: number) {
    await notificationApi.markAsRead(id)
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  async function markAllAsRead() {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
  }

  async function deleteNotification(id: number) {
    await notificationApi.deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
  }

  return {
    unreadCount,
    notifications,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification
  }
})
