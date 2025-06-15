import axios from 'axios';

const BASE_URL = '/api'; // 或者你的后端API基础路径

/**
 * 获取分配给维修人员的工单列表
 * @param {number} personnelId 维修人员ID
 * @param {string} status 工单分配状态 (Assigned, Accepted, Work_Completed, etc.)
 * @returns {Promise<any>}
 */
export const getAssignments = (personnelId, status) => {
  return axios.get(`${BASE_URL}/personnel/${personnelId}/assignments`, { params: { status } })
    .then(response => response.data);
};

/**
 * 更新工单分配的状态 (接受/拒绝)
 * @param {number} assignmentId 工单分配ID
 * @param {string} status 新的状态 ('Accepted' or 'Rejected')
 * @returns {Promise<any>}
 */
export const updateAssignmentStatus = (assignmentId, status) => {
  return axios.put(`${BASE_URL}/assignments/${assignmentId}/status`, { status })
    .then(response => response.data);
};

/**
 * 为工单分配记录工时
 * @param {number} assignmentId 工单分配ID
 * @param {number} hours 记录的小时数
 * @returns {Promise<any>}
 */
export const logHoursToAssignment = (assignmentId, hours) => {
  return axios.post(`${BASE_URL}/assignments/${assignmentId}/hours`, { hours })
    .then(response => response.data);
};

/**
 * 获取维修人员的个人资料
 * @param {number} personnelId 维修人员ID
 * @returns {Promise<any>}
 */
export const getPersonnelProfile = (personnelId) => {
  return axios.get(`${BASE_URL}/personnel/${personnelId}`)
    .then(response => response.data);
};

/**
 * 获取维修人员的薪资记录
 * @param {number} personnelId 维修人员ID
 * @returns {Promise<any>}
 */
export const getPayrollRecords = (personnelId) => {
  return axios.get(`${BASE_URL}/personnel/${personnelId}/payroll`)
    .then(response => response.data);
};

// 未来可以添加更多API调用，例如获取个人信息、薪资等 