import http from './http'

export const feedbackApi = {
  // 创建维修评价
  createFeedback(feedbackData) {
    return http.post('/feedbacks', feedbackData)
  },

  // 根据ID获取维修评价
  getFeedbackById(feedbackId) {
    return http.get(`/feedbacks/${feedbackId}`)
  },

  // 根据维修工单ID获取评价
  getFeedbackByOrderId(orderId) {
    return http.get(`/feedbacks/order/${orderId}`)
  },

  // 获取用户的所有评价
  getFeedbacksByUserId(userId) {
    return http.get(`/feedbacks/user/${userId}`)
  },

  // 更新评价
  updateFeedback(feedbackId, feedbackData) {
    return http.put(`/feedbacks/${feedbackId}`, feedbackData)
  },

  // 删除评价
  deleteFeedback(feedbackId) {
    return http.delete(`/feedbacks/${feedbackId}`)
  }
}

export default feedbackApi 