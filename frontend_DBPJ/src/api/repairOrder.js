import axios from 'axios';

const API_URL = '/api/repair-orders';

export const getOrderDetails = (orderId) => {
  return axios.get(`${API_URL}/${orderId}`)
    .then(response => response.data);
};

export const addMaterialToOrder = (orderId, materialId, quantity) => {
  return axios.post(`${API_URL}/${orderId}/materials`, {
    materialId,
    quantityUsed: quantity
  });
}; 