import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'

// 创建axios实例
const api: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 如果响应成功
    if (data.code === 200) {
      return data
    }
    
    // 显示错误消息
    ElMessage.error(data.message || '请求失败')
    
    // 如果是未认证错误，跳转到登录页
    if (data.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    // 处理网络错误
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未认证，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    
    return Promise.reject(error)
  }
)

// API方法
export const authApi = {
  login: (data: { username: string; password: string }) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  getCurrentUser: () => api.get('/auth/me')
}

export const userApi = {
  getUsers: (params?: any) => api.get('/users', { params }),
  getUser: (id: number) => api.get(`/users/${id}`),
  createUser: (data: any) => api.post('/users', data),
  updateUser: (id: number, data: any) => api.put(`/users/${id}`, data),
  deleteUser: (id: number) => api.delete(`/users/${id}`),
  resetPassword: (id: number, data: any) => api.post(`/users/${id}/reset-password`, data),
  changePassword: (id: number, data: any) => api.post(`/users/${id}/change-password`, data),
  uploadPhoto: (id: number, data: any) => api.post(`/users/${id}/upload-photo`, data),
  getSalespersons: () => api.get('/users/salespersons')
}

export const productApi = {
  getProducts: (params?: any) => api.get('/products', { params }),
  getProduct: (id: number) => api.get(`/products/${id}`),
  createProduct: (data: any) => api.post('/products', data),
  updateProduct: (id: number, data: any) => api.put(`/products/${id}`, data),
  deleteProduct: (id: number) => api.delete(`/products/${id}`),
  uploadImage: (id: number, data: any) => api.post(`/products/${id}/upload-image`, data),
  searchProducts: (keyword: string) => api.get('/products/search', { params: { keyword } }),
  getLowStockProducts: (threshold?: number) => api.get('/products/low-stock', { params: { threshold } }),
  getHotProducts: (limit?: number) => api.get('/products/hot', { params: { limit } }),
  updateStock: (id: number, data: any) => api.post(`/products/${id}/update-stock`, data)
}

export const orderApi = {
  getOrders: (params?: any) => api.get('/orders', { params }),
  getOrder: (id: number) => api.get(`/orders/${id}`),
  createOrder: (data: any) => api.post('/orders', data),
  updateOrder: (id: number, data: any) => api.put(`/orders/${id}`, data),
  deleteOrder: (id: number) => api.delete(`/orders/${id}`),
  updateOrderStatus: (id: number, data: any) => api.put(`/orders/${id}/status`, data),
  exportOrderPdf: (id: number) => api.get(`/orders/${id}/export-pdf`, { responseType: 'blob' }),
  completeOrder: (id: number) => api.post(`/orders/${id}/complete`),
  cancelOrder: (id: number, data: any) => api.post(`/orders/${id}/cancel`, data),
  getStatusCount: () => api.get('/orders/status-count')
}

export const overviewApi = {
  getStatistics: () => api.get('/overview/statistics'),
  getSalesRanking: (limit?: number) => api.get('/overview/sales-ranking', { params: { limit } }),
  getHotProducts: (limit?: number) => api.get('/overview/hot-products', { params: { limit } }),
  getOrderTrend: (days?: number) => api.get('/overview/order-trend', { params: { days } }),
  exportOverviewPdf: () => api.get('/overview/export-pdf', { responseType: 'blob' })
}

export const aiApi = {
  chat: (data: { question: string; conversationId?: string }) => api.post('/ai/chat', data),
  getConversationHistory: (conversationId: string) => api.get(`/ai/conversation/${conversationId}`),
  clearConversationHistory: (conversationId: string) => api.delete(`/ai/conversation/${conversationId}`),
  getIntents: () => api.get('/ai/intents'),
  analyzeData: (data: { question: string; data: any[] }) => api.post('/ai/analyze', data)
}

export const markdownDocumentApi = {
  getDocuments: (params?: any) => api.get('/markdown-documents', { params }),
  getDocument: (id: number) => api.get(`/markdown-documents/${id}`),
  uploadDocument: (data: any) => api.post('/markdown-documents', data),
  updateDocument: (id: number, data: any) => api.put(`/markdown-documents/${id}`, data),
  deleteDocument: (id: number) => api.delete(`/markdown-documents/${id}`),
  getDocumentContent: (id: number) => api.get(`/markdown-documents/${id}/content`),
  getDocumentsByUser: (userId: number) => api.get(`/markdown-documents/user/${userId}`)
}

export default api