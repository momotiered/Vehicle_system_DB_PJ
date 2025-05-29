import request from './http'

const vehicleApi = {
  // 获取所有车辆
  getAllVehicles(params = {}) {
    return request({
      method: 'GET',
      url: '/api/vehicles',
      params
    })
  },

  // 根据ID获取车辆
  getVehicleById(id) {
    return request({
      method: 'GET',
      url: `/api/vehicles/${id}`
    })
  },

  // 创建车辆
  createVehicle(data) {
    return request({
      method: 'POST',
      url: '/api/vehicles',
      data
    })
  },

  // 更新车辆
  updateVehicle(id, data) {
    return request({
      method: 'PUT',
      url: `/api/vehicles/${id}`,
      data
    })
  },

  // 删除车辆
  deleteVehicle(id) {
    return request({
      method: 'DELETE',
      url: `/api/vehicles/${id}`
    })
  },

  // 根据用户ID获取车辆列表
  getVehiclesByUserId(userId) {
    return request({
      method: 'GET',
      url: `/api/vehicles/user/${userId}`
    })
  }
}

export default vehicleApi 