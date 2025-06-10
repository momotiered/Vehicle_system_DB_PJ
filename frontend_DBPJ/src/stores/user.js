import { defineStore } from 'pinia'
import { ref } from 'vue'
import userApi from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  // 初始化时从localStorage恢复用户信息
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    try {
      user.value = JSON.parse(storedUser)
    } catch (error) {
      console.error('Failed to parse stored user data:', error)
      localStorage.removeItem('user')
    }
  }

  // 设置用户信息
  const setUser = (userData) => {
    user.value = userData
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData))
    } else {
      localStorage.removeItem('user')
    }
  }

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  // 登录
  const login = async (loginData) => {
    try {
      const response = await userApi.login(loginData)
      if (response && response.token) {
        setToken(response.token)
        setUser(response.user)
        
        // 验证用户角色是否与预期匹配
        if (loginData.expectedRole && response.user.role !== loginData.expectedRole) {
          logout()
          throw new Error('身份验证失败：您选择的登录身份与账户实际身份不符')
        }
        
        return true
      }
      return false
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  // 注册
  const register = async (userData) => {
    try {
      await userApi.createUser(userData)
      return true
    } catch (error) {
      console.error('注册失败:', error)
      return false
    }
  }

  // 登出
  const logout = () => {
    setUser(null)
    setToken('')
  }

  // 获取当前用户信息
  const getCurrentUser = async () => {
    if (!token.value) return null
    
    try {
      // 如果已经有用户信息，直接返回
      if (user.value) return user.value
      
      // 否则从API获取用户信息
      const userData = await userApi.getUserById(user.value?.userId)
      setUser(userData)
      return userData
    } catch (error) {
      console.error('获取用户信息失败:', error)
      logout()
      return null
    }
  }

  // 检查用户权限
  const hasRole = (requiredRole) => {
    return user.value?.role === requiredRole
  }

  // 检查是否为管理员
  const isAdmin = () => {
    return user.value?.role === 'ADMIN'
  }

  // 检查是否为技术人员
  const isTechnician = () => {
    return user.value?.role === 'TECHNICIAN'
  }

  // 检查是否为普通用户
  const isUser = () => {
    return user.value?.role === 'USER'
  }

  return {
    user,
    token,
    setUser,
    setToken,
    login,
    register,
    logout,
    getCurrentUser,
    hasRole,
    isAdmin,
    isTechnician,
    isUser
  }
}) 