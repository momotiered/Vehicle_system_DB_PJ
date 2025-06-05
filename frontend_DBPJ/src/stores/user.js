import { defineStore } from 'pinia'
import { ref } from 'vue'
import userApi from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  // 设置用户信息
  const setUser = (userData) => {
    user.value = userData
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
        return true
      }
      return false
    } catch (error) {
      console.error('登录失败:', error)
      return false
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

  return {
    user,
    token,
    setUser,
    setToken,
    login,
    register,
    logout,
    getCurrentUser
  }
}) 