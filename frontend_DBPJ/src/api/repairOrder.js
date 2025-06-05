import http from './http'

export const repairOrderApi = {
  // 创建维修工单
  createRepairOrder(orderData) {
    return http.post('/repair-orders', orderData)
  },

  // 用户提交维修申请
  createUserRepairRequest(requestData) {
    return http.post('/repair-orders/user-request', requestData)
  },

  // 根据ID获取维修工单
  getRepairOrderById(orderId) {
    return http.get(`/repair-orders/${orderId}`)
  },

  // 根据用户ID获取维修工单列表
  getRepairOrdersByUserId(userId) {
    return http.get(`/repair-orders/user/${userId}`)
  },

  // 根据车辆ID获取维修工单列表
  getRepairOrdersByVehicleId(vehicleId) {
    return http.get(`/repair-orders/vehicle/${vehicleId}`)
  },

  // 根据状态获取维修工单列表
  getRepairOrdersByStatus(status) {
    return http.get(`/repair-orders/status/${status}`)
  },

  // 根据日期范围获取维修工单列表
  getRepairOrdersByDateRange(startDate, endDate) {
    return http.get('/repair-orders/date-range', {
      params: { startDate, endDate }
    })
  },

  // 更新维修工单状态
  updateRepairOrderStatus(orderId, newStatus) {
    return http.put(`/repair-orders/${orderId}/status`, null, {
      params: { newStatus }
    })
  },

  // 更新维修工单信息
  updateRepairOrder(orderId, orderData) {
    return http.put(`/repair-orders/${orderId}`, orderData)
  },

  // 删除维修工单
  deleteRepairOrder(orderId) {
    return http.delete(`/repair-orders/${orderId}`)
  }
}

export default repairOrderApi 