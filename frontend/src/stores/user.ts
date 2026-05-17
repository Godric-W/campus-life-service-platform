import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserProfile } from '@/types'
import { authApi, userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token') || null)
  const userInfo = ref<UserProfile | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(account: string, password: string) {
    const response = await authApi.login({ account, password })
    const rawToken = response.data.token
    const cleanToken = rawToken?.startsWith('Bearer ') ? rawToken.substring(7) : rawToken
    token.value = cleanToken
    localStorage.setItem('token', cleanToken!)
    userInfo.value = {
      userId: response.data.user.userId,
      studentNo: response.data.user.studentNo,
      username: response.data.user.username,
      phone: '',
      email: '',
      college: '',
      major: '',
      avatar: '',
      role: response.data.user.role,
      status: response.data.user.status,
      createdAt: '',
      updatedAt: ''
    }
    return response
  }

  async function register(data: {
    studentNo: string
    username: string
    password: string
    phone: string
    email: string
    college: string
    major: string
  }) {
    return await authApi.register(data)
  }

  async function logout() {
    await authApi.logout()
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const response = await userApi.getUserInfo()
      userInfo.value = response.data
    } catch {
      token.value = null
      localStorage.removeItem('token')
    }
  }

  async function updateUserInfo(data: Partial<UserProfile>) {
    const response = await userApi.updateUserInfo(data)
    userInfo.value = response.data
    return response
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    logout,
    fetchUserInfo,
    updateUserInfo
  }
})
