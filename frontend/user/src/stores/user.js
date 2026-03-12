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

  // 已保存的账号列表
  const savedAccounts = ref(loadSavedAccounts())

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const isArtist = computed(() => user.value?.role === 'ARTIST')

  // 加载已保存的账号列表
  function loadSavedAccounts() {
    try {
      const stored = localStorage.getItem('savedAccounts')
      if (stored && stored !== 'undefined' && stored !== 'null') {
        return JSON.parse(stored)
      }
    } catch (e) {
      console.error('解析已保存账号失败:', e)
      localStorage.removeItem('savedAccounts')
    }
    return []
  }

  // 保存当前账号到已保存列表
  function saveCurrentAccount() {
    if (!token.value || !user.value) return
    const account = {
      id: user.value.id,
      username: user.value.username,
      avatarUrl: user.value.avatarUrl,
      role: user.value.role,
      token: token.value,
      refreshToken: refreshTokenValue.value,
      user: { ...user.value }
    }
    // 去重：用 id 标识
    const list = savedAccounts.value.filter(a => a.id !== account.id)
    list.unshift(account)
    // 最多保存5个
    savedAccounts.value = list.slice(0, 5)
    localStorage.setItem('savedAccounts', JSON.stringify(savedAccounts.value))
  }

  // 切换到已保存的账号
  function switchToAccount(account) {
    // 先保存当前账号
    saveCurrentAccount()
    // 切换
    token.value = account.token
    refreshTokenValue.value = account.refreshToken
    user.value = account.user
    localStorage.setItem('token', account.token)
    localStorage.setItem('user', JSON.stringify(account.user))
    if (account.refreshToken) {
      localStorage.setItem('refreshToken', account.refreshToken)
    }
  }

  // 从已保存列表移除账号
  function removeSavedAccount(accountId) {
    savedAccounts.value = savedAccounts.value.filter(a => a.id !== accountId)
    localStorage.setItem('savedAccounts', JSON.stringify(savedAccounts.value))
  }

  // 获取其他已保存的账号（排除当前）
  function getOtherAccounts() {
    if (!user.value) return savedAccounts.value
    return savedAccounts.value.filter(a => a.id !== user.value.id)
  }

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
    // 登录成功后自动保存到账号列表
    saveCurrentAccount()
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
    // 同步更新保存的账号列表中的信息
    saveCurrentAccount()
  }

  return {
    token,
    refreshToken: refreshTokenValue,
    user,
    savedAccounts,
    isAuthenticated,
    isArtist,
    setAuth,
    clearAuth,
    updateUser,
    saveCurrentAccount,
    switchToAccount,
    removeSavedAccount,
    getOtherAccounts
  }
})
