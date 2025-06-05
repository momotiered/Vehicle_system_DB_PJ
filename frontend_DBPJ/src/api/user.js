import http from './http'

export const userApi = {
  // 创建用户
  createUser(userData) {
    return http.post('/users', userData)
  },

  // 根据ID获取用户
  getUserById(userId) {
    return http.get(`/users/${userId}`)
  },

  // 获取所有用户
  getAllUsers() {
    return http.get('/users')
  },

  // 更新用户信息
  updateUser(userId, userData) {
    return http.put(`/users/${userId}`, userData)
  },

  // 删除用户
  deleteUser(userId) {
    return http.delete(`/users/${userId}`)
  },

  // 登录
  login(loginData) {
    return http.post('/auth/login', loginData)
  }
}

export default userApi 