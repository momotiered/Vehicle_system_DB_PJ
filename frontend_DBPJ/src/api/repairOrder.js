import axios from 'axios';

const API_URL = '/api/repair-orders';
const MATERIALS_URL = '/api/materials'; // Assuming this is the endpoint for materials

export const getOrderDetails = (orderId) => {
  return axios.get(`${API_URL}/${orderId}`)
    .then(response => response.data);
};

export const addMaterialToOrder = (orderId, materialId, quantity) => {
  return axios.post(`${API_URL}/${orderId}/materials`, {
    materialId,
    quantity: quantity
  });
};

export const getMaterials = () => {
    return axios.get(MATERIALS_URL).then(response => response.data);
} 