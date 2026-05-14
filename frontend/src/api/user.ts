import axios from '@/utils/axios'
import type { ApiResponse, UserProfile } from '@/types'

export const userApi = {
  async getUserInfo(): Promise<ApiResponse<UserProfile>> {
    const response = await axios.get('/users/me')
    return response.data
  },

  async updateUserInfo(data: Partial<UserProfile>): Promise<ApiResponse<UserProfile>> {
    const response = await axios.put('/users/me', data)
    return response.data
  },

  async getUserById(id: number): Promise<ApiResponse<UserProfile>> {
    const response = await axios.get(`/users/${id}`)
    return response.data
  }
}
