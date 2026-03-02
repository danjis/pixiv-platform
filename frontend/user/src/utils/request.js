import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Token 刷新状态管理
let isRefreshing = false
let failedQueue = []

function processQueue(error, token = null) {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  failedQueue = []
}

// 请求拦截器：添加 JWT 令牌
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    
    // 如果存在 token，添加到请求头（Gateway 会从 JWT 解析出 userId 和 Role）
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理错误和令牌过期（支持自动刷新）
request.interceptors.response.use(
  (response) => {
    // 直接返回响应数据
    return response.data
  },
  async (error) => {
    const userStore = useUserStore()
    const originalRequest = error.config
    
    if (error.response) {
      const { status, data } = error.response
      
      // 401 且有 refreshToken 且不是刷新请求本身 => 尝试自动刷新
      if (status === 401 && userStore.refreshToken && !originalRequest._retry) {
        if (isRefreshing) {
          // 已经在刷新中，排队等待
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return request(originalRequest)
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          // 直接用 axios 发请求避免被拦截器循环
          const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
          const res = await axios.post(`${baseURL}/api/auth/refresh`, {
            refreshToken: userStore.refreshToken
          })
          
          const resData = res.data
          if (resData.code === 200 && resData.data?.accessToken) {
            const newToken = resData.data.accessToken
            const newRefreshToken = resData.data.refreshToken
            
            // 更新存储
            userStore.setAuth({
              token: newToken,
              refreshToken: newRefreshToken,
              user: userStore.user
            })
            
            // 重试排队的请求
            processQueue(null, newToken)
            
            // 重试原始请求
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return request(originalRequest)
          } else {
            processQueue(new Error('refresh failed'))
            userStore.clearAuth()
            ElMessage.error('登录已过期，请重新登录')
            router.push({ name: 'Login' })
          }
        } catch (refreshError) {
          processQueue(refreshError)
          userStore.clearAuth()
          ElMessage.error('登录已过期，请重新登录')
          router.push({ name: 'Login' })
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      } else if (status === 401) {
        // 无 refreshToken 或刷新失败
        ElMessage.error('登录已过期，请重新登录')
        userStore.clearAuth()
        router.push({ name: 'Login' })
      } else {
        switch (status) {
          case 403:
            ElMessage.error('没有权限访问该资源')
            break
          case 404:
            ElMessage.error('请求的资源不存在')
            break
          case 500:
            ElMessage.error(data?.message || '服务器错误，请稍后重试')
            break
          default:
            ElMessage.error(data?.message || `请求失败 (${status})`)
        }
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 其他错误
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request
