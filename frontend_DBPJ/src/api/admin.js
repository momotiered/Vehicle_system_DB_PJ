import axios from 'axios';

const API_BASE_URL = '/api/admin';

/**
 * 获取所有待分配的工单
 * @returns {Promise<any>}
 */
export const getPendingOrders = () => {
    return axios.get(`${API_BASE_URL}/orders/pending`)
        .then(response => response.data);
};

/**
 * 获取所有可用的维修人员
 * @returns {Promise<any>}
 */
export const getAvailablePersonnel = () => {
    return axios.get(`${API_BASE_URL}/personnel/available`)
        .then(response => response.data);
};

/**
 * 为指定工单分配维修人员
 * @param {number} orderId 工单ID
 * @param {number[]} personnelIds 维修人员ID列表
 * @returns {Promise<any>}
 */
export const assignOrder = (orderId, personnelIds) => {
    return axios.post(`${API_BASE_URL}/orders/${orderId}/assign`, { personnelIds });
};

// --- User Management ---

export const getAllUsers = () => {
    return axios.get('/api/admin/users');
};

export const createUser = (userData) => {
    return axios.post('/api/admin/users', userData);
};

export const updateUser = (id, userData) => {
    return axios.put(`/api/admin/users/${id}`, userData);
};

export const deleteUser = (id) => {
    return axios.delete(`/api/admin/users/${id}`);
};

// --- Repair Personnel Management ---

export const getAllPersonnel = () => {
    return axios.get('/api/admin/personnel');
};

export const createPersonnel = (personnelData) => {
    return axios.post('/api/admin/personnel', personnelData);
};

export const updatePersonnel = (id, personnelData) => {
    return axios.put(`/api/admin/personnel/${id}`, personnelData);
};

export const deletePersonnel = (id) => {
    return axios.delete(`/api/admin/personnel/${id}`);
};

// --- 监控面板 API ---

export const getOverviewStats = () => {
    return axios.get('/api/admin/monitor/overview');
};

export const getOrderStatusStats = () => {
    return axios.get('/api/admin/monitor/order-status');
};

export const getFinancialStats = () => {
    return axios.get('/api/admin/monitor/financial');
};

export const getInventoryAlerts = () => {
    return axios.get('/api/admin/monitor/inventory-alerts');
};

export const getPersonnelStats = () => {
    return axios.get('/api/admin/monitor/personnel');
};

export const getSatisfactionStats = () => {
    return axios.get('/api/admin/monitor/satisfaction');
};

// --- Vehicle Management ---
export const getAllVehicles = () => {
    return axios.get('/api/admin/vehicles');
};

export const getVehicleById = (id) => {
    return axios.get(`/api/admin/vehicles/${id}`);
};

export const updateVehicle = (id, vehicleData) => {
    return axios.put(`/api/admin/vehicles/${id}`, vehicleData);
};

export const deleteVehicle = (id) => {
    return axios.delete(`/api/admin/vehicles/${id}`);
}; 