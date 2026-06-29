import axios from 'axios'

// Axios instance — baseURL uses the Vite proxy (see vite.config.js)
// All requests go to /api/... which Vite forwards to http://localhost:8080/...
const api = axios.create({
  baseURL: '/api',
  timeout: 8000,
  headers: { 'Content-Type': 'application/json' },
})

// ── Request interceptor (add auth token here later when you add JWT) ──
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ── Response interceptor (centralised error handling) ──
api.interceptors.response.use(
  res => res,
  err => {
    const msg = err.response?.data?.message || err.message || 'Something went wrong'
    return Promise.reject(new Error(msg))
  }
)

// ── Product endpoints ──
export const productService = {
  getAll: ()           => api.get('/products').then(r => r.data),
  getById: (id)        => api.get(`/products/${id}`).then(r => r.data),
  getByCategory: (cat) => api.get(`/products?category=${cat}`).then(r => r.data),
}

// ── Order endpoints ──
export const orderService = {
  placeOrder: (payload) => api.post('/orders', payload).then(r => r.data),
  getByUser:  (userId)  => api.get(`/orders/user/${userId}`).then(r => r.data),
}

// ── User / Auth endpoints ──
export const userService = {
  register: (data)  => api.post('/users/register', data).then(r => r.data),
  login:    (data)  => api.post('/users/login', data).then(r => r.data),
}

export default api
