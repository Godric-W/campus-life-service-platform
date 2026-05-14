import axios from '@/utils/axios'
import type { ApiResponse, PageResponse, Notification } from '@/types'

export const notificationApi = {
  async getNotifications(pageNum: number, pageSize: number): Promise<PageResponse<Notification>> {
    const response = await axios.get('/notifications', {
      params: { pageNum, pageSize }
    })
    return response.data
  },

  async getUnreadCount(): Promise<ApiResponse<number>> {
    const response = await axios.get('/notifications/unread-count')
    return response.data
  },

  async markAsRead(id: number): Promise<ApiResponse<void>> {
    const response = await axios.put(`/notifications/${id}/read`)
    return response.data
  },

  async markAllAsRead(): Promise<ApiResponse<void>> {
    const response = await axios.put('/notifications/read-all')
    return response.data
  },

  async deleteNotification(id: number): Promise<ApiResponse<void>> {
    const response = await axios.delete(`/notifications/${id}`)
    return response.data
  }
}
