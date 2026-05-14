import axios from '@/utils/axios'
import type { ApiResponse, PageResponse, Club, Activity } from '@/types'

export const activityApi = {
  async getClubs(): Promise<ApiResponse<Club[]>> {
    const response = await axios.get('/clubs')
    return response.data
  },

  async getClubById(id: number): Promise<ApiResponse<Club>> {
    const response = await axios.get(`/clubs/${id}`)
    return response.data
  },

  async createClub(data: {
    name: string
    description: string
    logo?: string
    contactInfo: string
  }): Promise<ApiResponse<number>> {
    const response = await axios.post('/clubs', data)
    return response.data
  },

  async updateClub(id: number, data: {
    name: string
    description: string
    logo?: string
    contactInfo: string
  }): Promise<ApiResponse<void>> {
    const response = await axios.put(`/clubs/${id}`, data)
    return response.data
  },

  async getMyClubs(): Promise<ApiResponse<Club[]>> {
    const response = await axios.get('/clubs/my')
    return response.data
  },

  async getActivities(params: {
    pageNum?: number
    pageSize?: number
    keyword?: string
    status?: string
  }): Promise<PageResponse<Activity>> {
    const response = await axios.get('/activities', { params })
    return response.data
  },

  async getActivityById(id: number): Promise<ApiResponse<Activity>> {
    const response = await axios.get(`/activities/${id}`)
    return response.data
  },

  async createActivity(data: {
    clubId: number
    title: string
    description: string
    location: string
    coverImage?: string
    startTime: string
    endTime: string
    signupDeadline: string
    maxParticipants: number
  }): Promise<ApiResponse<number>> {
    const response = await axios.post('/activities', data)
    return response.data
  },

  async registerActivity(id: number): Promise<ApiResponse<void>> {
    const response = await axios.post(`/activities/${id}/register`)
    return response.data
  },

  async cancelRegistration(id: number): Promise<ApiResponse<void>> {
    const response = await axios.delete(`/activities/${id}/register`)
    return response.data
  },

  async cancelActivity(id: number): Promise<ApiResponse<void>> {
    const response = await axios.put(`/activities/${id}/cancel`)
    return response.data
  },

  async getMyRegisteredActivities(params: {
    pageNum?: number
    pageSize?: number
  }): Promise<PageResponse<Activity>> {
    const response = await axios.get('/activities/my-registered', { params })
    return response.data
  },

  async getMyPublishedActivities(params: {
    pageNum?: number
    pageSize?: number
  }): Promise<PageResponse<Activity>> {
    const response = await axios.get('/activities/my-published', { params })
    return response.data
  }
}
