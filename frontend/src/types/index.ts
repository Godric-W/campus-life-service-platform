export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T = any> {
  code: number
  message: string
  data: {
    records: T[]
    total: number
    pageNum: number
    pageSize: number
  }
}

export interface LoginRequest {
  account: string
  password: string
}

export interface RegisterRequest {
  studentNo: string
  username: string
  password: string
  phone: string
  email: string
  college: string
  major: string
}

export interface LoginResponse {
  token: string
  expireSeconds: number
  user: {
    userId: number
    studentNo: string
    username: string
    role: string
    status: number
  }
}

export interface UserProfile {
  userId: number
  studentNo: string
  username: string
  phone: string
  email: string
  college: string
  major: string
  avatar: string
  role: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface MarketItem {
  id: number
  title: string
  description: string
  price: number
  category: string
  coverImage: string
  images: string
  contactInfo: string
  status: string
  publisherId: number
  publisherName: string
  createTime: string
  updateTime: string
}

export interface PublishItemRequest {
  title: string
  description: string
  price: number
  category: string
  coverImage?: string
  images?: string
  contactInfo: string
}

export interface HelpTask {
  id: number
  title: string
  description: string
  taskType: string
  reward: number
  pickupAddress: string
  deliveryAddress: string
  deadline: string
  contactInfo: string
  status: string
  publisherId: number
  publisherName: string
  accepterId?: number
  accepterName?: string
  createTime: string
  updateTime: string
}

export interface PublishTaskRequest {
  title: string
  description: string
  taskType: string
  reward: number
  pickupAddress: string
  deliveryAddress: string
  deadline: string
  contactInfo: string
}

export interface Club {
  id: number
  name: string
  description: string
  logo: string
  contactInfo: string
  adminId: number
  adminName: string
  createTime: string
  updateTime: string
}

export interface Activity {
  id: number
  clubId: number
  clubName: string
  title: string
  description: string
  location: string
  coverImage: string
  startTime: string
  endTime: string
  signupDeadline: string
  maxParticipants: number
  currentParticipants: number
  status: string
  publisherId: number
  publisherName: string
  createTime: string
  updateTime: string
}

export interface Notification {
  id: number
  receiverId: number
  title: string
  content: string
  type: string
  businessId?: number
  businessType?: string
  readStatus: number
  createTime: string
}
