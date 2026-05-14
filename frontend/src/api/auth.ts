import axios from '@/utils/axios'
import type { ApiResponse, LoginRequest, LoginResponse, RegisterRequest } from '@/types'

export const authApi = {
  async login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    const response = await axios.post('/auth/login', data)
    return response.data
  },

  async register(data: RegisterRequest): Promise<ApiResponse<void>> {
    const response = await axios.post('/auth/register', data)
    return response.data
  },

  async logout(): Promise<ApiResponse<void>> {
    const response = await axios.post('/auth/logout')
    return response.data
  }
}
