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

export const uploadApi = {
  uploadImage: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/upload/image', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  uploadFile: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/upload/file', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  }
}

export const userApi = {
  getUsers: (params?: any) => api.get('/users', { params }),
  getUser: (id: number) => api.get(`/users/${id}`),
  createUser: (data: any) => api.post('/users', data),
  updateUser: (id: number, data: any) => api.put(`/users/${id}`, data),
  deleteUser: (id: number) => api.delete(`/users/${id}`),
  updateUserStatus: (id: number, status: number) => api.put(`/users/${id}`, { status }),
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
  downloadOrderPdf: (id: number) => api.get(`/orders/${id}/export-pdf`, { responseType: 'blob' }),
  completeOrder: (id: number) => api.post(`/orders/${id}/complete`),
  cancelOrder: (id: number, data: any) => api.post(`/orders/${id}/cancel`, data),
  getStatusCount: () => api.get('/orders/status-count')
}

export const overviewApi = {
  getStats: () => api.get('/overview/statistics'),
  getStatistics: () => api.get('/overview/statistics'),
  getTopSales: (limit?: number) => api.get('/overview/sales-ranking', { params: { limit } }),
  getSalesRanking: (limit?: number) => api.get('/overview/sales-ranking', { params: { limit } }),
  getHotProducts: (limit?: number) => api.get('/overview/hot-products', { params: { limit } }),
  getRecentOrders: (limit?: number) => api.get('/overview/recent-orders', { params: { limit } }),
  getSalesTrend: (period?: string) => {
    const daysMap: Record<string, number> = { week: 7, month: 30, year: 365 }
    const days = daysMap[period || 'month'] || 30
    return api.get('/overview/order-trend', { params: { days } })
  },
  getOrderTrend: (days?: number) => api.get('/overview/order-trend', { params: { days } }),
  getOrderStatusDistribution: () => api.get('/orders/status-count'),
  printReport: () => api.get('/overview/export-pdf', { responseType: 'blob' }),
  exportOverviewPdf: () => api.get('/overview/export-pdf', { responseType: 'blob' })
}

export const aiApi = {
  chat: (data: { question: string; conversationId?: string; mode?: string }) => api.post('/ai/chat', data),
  
  // SSE流式聊天
  chatStream: (data: { question: string; conversationId?: string }, onToken: (token: string) => void, onComplete: (response: any) => void, onError: (error: string) => void) => {
    const userStore = useUserStore()
    
    fetch('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.token}`
      },
      body: JSON.stringify(data)
    }).then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const reader = response.body?.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      
      const processStream = () => {
        reader?.read().then(({ done, value }) => {
          if (done) {
            return
          }
          
          buffer += decoder.decode(value, { stream: true })
          
          // 处理SSE事件
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''
          
          let currentEvent = ''
          
          for (const line of lines) {
            if (line.startsWith('event:')) {
              currentEvent = line.substring(6).trim()
            } else if (line.startsWith('data:')) {
              const data = line.substring(5).trim()
              
              try {
                switch (currentEvent) {
                  case 'token':
                    onToken(data)
                    break
                  case 'complete':
                    onComplete(JSON.parse(data))
                    break
                  case 'error':
                    onError(data)
                    break
                  case 'conversationId':
                    // 对话ID已在data中
                    break
                }
              } catch (e) {
                console.error('解析SSE数据失败:', e, data)
              }
              
              currentEvent = ''
            }
          }
          
          processStream()
        }).catch(error => {
          onError(error.message)
        })
      }
      
      processStream()
    }).catch(error => {
      onError(error.message)
    })
  },
  
  getConversationHistory: (conversationId: string) => api.get(`/ai/conversation/${conversationId}`),
  clearConversationHistory: (conversationId: string) => api.delete(`/ai/conversation/${conversationId}`),
  getConversations: () => api.get('/ai/conversations'),
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