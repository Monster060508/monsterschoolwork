import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 用户状态管理
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<any>(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => userInfo.value?.role || '')
  const userName = computed(() => userInfo.value?.name || '')

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: any) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    localStorage.setItem('userRole', info.role)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userRole')
  }

  function isAdmin() {
    return userRole.value === 'ADMIN'
  }

  function isSalesperson() {
    return userRole.value === 'SALES'
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userRole,
    userName,
    setToken,
    setUserInfo,
    logout,
    isAdmin,
    isSalesperson
  }
})

// 应用状态管理
export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const loading = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setLoading(status: boolean) {
    loading.value = status
  }

  return {
    sidebarCollapsed,
    loading,
    toggleSidebar,
    setLoading
  }
})