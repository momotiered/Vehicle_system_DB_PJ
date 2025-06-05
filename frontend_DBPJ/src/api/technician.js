import http from './http';

// 获取所有技术人员
export const getAllTechnicians = () => {
  return http.get('/technicians');
};

// 获取特定技术人员
export const getTechnicianById = (technicianId) => {
  return http.get(`/technicians/${technicianId}`);
};

// 获取用户对应的技术人员信息
export const getTechnicianByUserId = (userId) => {
  return http.get(`/technicians/user/${userId}`);
};

// 获取特定技能的技术人员
export const getTechniciansBySkill = (skillType) => {
  return http.get(`/technicians/skill/${skillType}`);
};

// 获取可用的技术人员
export const getAvailableTechnicians = () => {
  return http.get('/technicians/available');
};

// 创建技术人员
export const createTechnician = (technician, userId) => {
  return http.post(`/technicians?userId=${userId}`, technician);
};

// 更新技术人员信息
export const updateTechnician = (technicianId, technician) => {
  return http.put(`/technicians/${technicianId}`, technician);
};

// 更新技术人员可用状态
export const updateTechnicianAvailability = (technicianId, available) => {
  return http.put(`/technicians/${technicianId}/availability`, { available });
};

// 获取技术人员的工单分配
export const getTechnicianAssignments = (technicianId) => {
  return http.get(`/technicians/${technicianId}/assignments`);
};

// 获取技术人员特定状态的工单分配
export const getTechnicianAssignmentsByStatus = (technicianId, status) => {
  return http.get(`/technicians/${technicianId}/assignments/status/${status}`);
};

// 接受工单
export const acceptAssignment = (assignmentId) => {
  return http.put(`/technicians/assignments/${assignmentId}/accept`);
};

// 拒绝工单
export const rejectAssignment = (assignmentId, reason) => {
  return http.put(`/technicians/assignments/${assignmentId}/reject`, { reason });
};

// 完成工单
export const completeAssignment = (assignmentId, laborHours) => {
  return http.put(`/technicians/assignments/${assignmentId}/complete`, { laborHours });
}; 