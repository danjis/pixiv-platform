import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

function resolveApiBaseURL() {
  const configuredBaseURL = import.meta.env.VITE_API_BASE_URL?.trim()
  if (configuredBaseURL) {
    return configuredBaseURL
  }

  return import.meta.env.DEV ? 'http://localhost:8080' : '/'
}

const request = axios.create({
  baseURL: resolveApiBaseURL(),
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

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

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()

    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const userStore = useUserStore()
    const originalRequest = error.config

    if (error.response) {
      const { status, data } = error.response

      if (status === 401 && userStore.refreshToken && !originalRequest._retry) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return request(originalRequest)
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          const baseURL = resolveApiBaseURL()
          const res = await axios.post(`${baseURL}/api/auth/refresh`, {
            refreshToken: userStore.refreshToken
          })

          const resData = res.data
          if (resData.code === 200 && resData.data?.accessToken) {
            const newToken = resData.data.accessToken
            const newRefreshToken = resData.data.refreshToken

            userStore.setAuth({
              token: newToken,
              refreshToken: newRefreshToken,
              user: userStore.user
            })

            processQueue(null, newToken)

            originalRequest.headers.Authorization = `Bearer ${newToken}`
            return request(originalRequest)
          }

          processQueue(new Error('refresh failed'))
          userStore.clearAuth()
          ElMessage.error('登录已过期，请重新登录')
          router.push({ name: 'Login' })
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
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

export default request
