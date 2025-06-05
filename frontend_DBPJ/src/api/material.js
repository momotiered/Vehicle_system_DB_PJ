import http from './http';

// 获取所有维修材料
export const getAllMaterials = () => {
  return http.get('/materials');
};

// 获取特定维修材料
export const getMaterialById = (materialId) => {
  return http.get(`/materials/${materialId}`);
};

// 获取特定类别的材料
export const getMaterialsByCategory = (category) => {
  return http.get(`/materials/category/${category}`);
};

// 获取库存低的材料
export const getLowStockMaterials = (threshold = 10) => {
  return http.get(`/materials/low-stock?threshold=${threshold}`);
};

// 搜索材料
export const searchMaterials = (keyword) => {
  return http.get(`/materials/search?keyword=${keyword}`);
};

// 创建新材料
export const createMaterial = (material) => {
  return http.post('/materials', material);
};

// 更新材料信息
export const updateMaterial = (materialId, material) => {
  return http.put(`/materials/${materialId}`, material);
};

// 更新库存数量
export const updateStock = (materialId, quantity) => {
  return http.put(`/materials/${materialId}/stock`, { quantity });
};

// 增加库存
export const increaseStock = (materialId, amount) => {
  return http.put(`/materials/${materialId}/stock/increase`, { amount });
};

// 减少库存
export const decreaseStock = (materialId, amount) => {
  return http.put(`/materials/${materialId}/stock/decrease`, { amount });
};

// 删除材料
export const deleteMaterial = (materialId) => {
  return http.delete(`/materials/${materialId}`);
};

// 获取工单的材料使用记录
export const getUsagesByOrderId = (orderId) => {
  return http.get(`/material-usage/order/${orderId}`);
};

// 获取材料的使用记录
export const getUsagesByMaterialId = (materialId) => {
  return http.get(`/material-usage/material/${materialId}`);
};

// 获取技术人员的材料使用记录
export const getUsagesByTechnicianId = (technicianId) => {
  return http.get(`/material-usage/technician/${technicianId}`);
};

// 获取工单的材料总成本
export const getTotalMaterialCost = (orderId) => {
  return http.get(`/material-usage/order/${orderId}/total-cost`);
};

// 创建材料使用记录
export const createMaterialUsage = (materialUsage, orderId, materialId, technicianId) => {
  return http.post(`/material-usage?orderId=${orderId}&materialId=${materialId}&technicianId=${technicianId}`, materialUsage);
};

// 更新材料使用记录
export const updateMaterialUsage = (usageId, materialUsage) => {
  return http.put(`/material-usage/${usageId}`, materialUsage);
};

// 删除材料使用记录
export const deleteMaterialUsage = (usageId) => {
  return http.delete(`/material-usage/${usageId}`);
}; 