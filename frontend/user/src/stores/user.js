import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || null)
  const refreshTokenValue = ref(localStorage.getItem('refreshToken') || null)
  
  // 安全地解析 localStorage 中的用户数据
  let userData = null
  try {
    const storedUser = localStorage.getItem('user')
    if (storedUser && storedUser !== 'undefined' && storedUser !== 'null') {
      userData = JSON.parse(storedUser)
    }
  } catch (error) {
    console.error('解析用户数据失败:', error)
    localStorage.removeItem('user')
  }
  const user = ref(userData)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const isArtist = computed(() => user.value?.role === 'ARTIST')

  // 方法
  function setAuth(authData) {
    token.value = authData.token
    user.value = authData.user
    
    // 持久化到 localStorage
    localStorage.setItem('token', authData.token)
    localStorage.setItem('user', JSON.stringify(authData.user))
    if (authData.refreshToken) {
      refreshTokenValue.value = authData.refreshToken
      localStorage.setItem('refreshToken', authData.refreshToken)
    }
  }

  function clearAuth() {
    token.value = null
    user.value = null
    refreshTokenValue.value = null
    
    // 清除 localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('refreshToken')
  }

  function updateUser(userData) {
    user.value = { ...user.value, ...userData }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return {
    token,
    refreshToken: refreshTokenValue,
    user,
    isAuthenticated,
    isArtist,
    setAuth,
    clearAuth,
    updateUser
  }
})
