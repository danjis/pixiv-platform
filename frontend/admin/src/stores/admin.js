import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  // 状态
  const adminInfo = ref(null)
  const token = ref(localStorage.getItem('admin_token') || '')

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 方法
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('admin_token', newToken)
    } else {
      localStorage.removeItem('admin_token')
    }
  }

  function setAdminInfo(info) {
    adminInfo.value = info
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
  }

  return {
    adminInfo,
    token,
    isLoggedIn,
    setToken,
    setAdminInfo,
    logout
  }
})
