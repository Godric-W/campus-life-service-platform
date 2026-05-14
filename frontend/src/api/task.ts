import axios from '@/utils/axios'
import type { ApiResponse, PageResponse, HelpTask, PublishTaskRequest } from '@/types'

export const taskApi = {
  async getTasks(params: {
    pageNum?: number
    pageSize?: number
    keyword?: string
    taskType?: string
    status?: string
  }): Promise<PageResponse<HelpTask>> {
    const response = await axios.get('/tasks', { params })
    return response.data
  },

  async getTaskById(id: number): Promise<ApiResponse<HelpTask>> {
    const response = await axios.get(`/tasks/${id}`)
    return response.data
  },

  async createTask(data: PublishTaskRequest): Promise<ApiResponse<HelpTask>> {
    const response = await axios.post('/tasks', data)
    return response.data
  },

  async acceptTask(id: number): Promise<ApiResponse<void>> {
    const response = await axios.put(`/tasks/${id}/accept`)
    return response.data
  },

  async completeTask(id: number): Promise<ApiResponse<void>> {
    const response = await axios.put(`/tasks/${id}/complete`)
    return response.data
  },

  async cancelTask(id: number): Promise<ApiResponse<void>> {
    const response = await axios.put(`/tasks/${id}/cancel`)
    return response.data
  },

  async getMyPublishedTasks(params: {
    pageNum?: number
    pageSize?: number
  }): Promise<PageResponse<HelpTask>> {
    const response = await axios.get('/tasks/my-published', { params })
    return response.data
  },

  async getMyAcceptedTasks(params: {
    pageNum?: number
    pageSize?: number
  }): Promise<PageResponse<HelpTask>> {
    const response = await axios.get('/tasks/my-accepted', { params })
    return response.data
  }
}
