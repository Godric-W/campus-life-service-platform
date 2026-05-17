import axios from '@/utils/axios'
import type { ApiResponse, PageResponse, MarketItem, PublishItemRequest } from '@/types'

export const marketApi = {
  async getMarketItems(params: {
    pageNum?: number
    pageSize?: number
    keyword?: string
    category?: string
    status?: string
  }): Promise<PageResponse<MarketItem>> {
    const response = await axios.get('/market/items', { params })
    return response.data
  },

  async getMarketItemById(id: number): Promise<ApiResponse<MarketItem>> {
    const response = await axios.get(`/market/items/${id}`)
    return response.data
  },

  async createMarketItem(data: PublishItemRequest): Promise<ApiResponse<MarketItem>> {
    const response = await axios.post('/market/items', data)
    return response.data
  },

  async updateMarketItem(id: number, data: Partial<PublishItemRequest>): Promise<ApiResponse<MarketItem>> {
    const response = await axios.put(`/market/items/${id}`, data)
    return response.data
  },

  async offShelfItem(id: number): Promise<ApiResponse<void>> {
    const response = await axios.put(`/market/items/${id}/off-shelf`)
    return response.data
  },

  async onShelfItem(id: number): Promise<ApiResponse<void>> {
    const response = await axios.put(`/market/items/${id}/on-shelf`)
    return response.data
  },

  async markAsSold(id: number): Promise<ApiResponse<void>> {
    const response = await axios.put(`/market/items/${id}/sold`)
    return response.data
  },

  async getMyItems(params: {
    pageNum?: number
    pageSize?: number
  }): Promise<PageResponse<MarketItem>> {
    const response = await axios.get('/market/items/my', { params })
    return response.data
  }
}
