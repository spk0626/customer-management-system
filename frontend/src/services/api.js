import axios from 'axios';
import { toast } from 'react-toastify';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1',
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
});

// Surface backend error messages consistently in the UI.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.errors?.join(', ') ||
      error.message ||
      'An unexpected error occurred';
    toast.error(message, { toastId: `api-error:${message}` });
    return Promise.reject(error);
  }
);

export const customerApi = {
  getAll: (page = 0, size = 20, sortBy = 'name', direction = 'asc') =>
    api.get('/customers', { params: { page, size, sortBy, direction } }),

  getById: (id) =>
    api.get(`/customers/${id}`),

  search: (query) =>
    api.get('/customers/search', { params: { q: query } }),

  create: (data) =>
    api.post('/customers', data),

  update: (id, data) =>
    api.put(`/customers/${id}`, data),

  delete: (id) =>
    api.delete(`/customers/${id}`),
};

export const cityApi = {
  search: (query) =>
    api.get('/cities/search', { params: { q: query } }),
};

export const bulkApi = {
  upload: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/bulk/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000,
    });
  },

  getJobStatus: (jobId) =>
    api.get(`/bulk/jobs/${jobId}`),
};

export default api;
