import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 8000,
  headers: { 'Content-Type': 'application/json' },
})

// Attach JWT to every request automatically
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

// Centralised error handling
api.interceptors.response.use(
  res => res,
  err => {
    const msg =
      err.response?.data?.message ||
      err.message ||
      'Something went wrong'

    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('shoplane_user')
      window.location.href = '/login'
    }

    return Promise.reject(new Error(msg))
  }
)

// Products
export const productService = {
  getAll: () =>
    api.get('/products').then(r => r.data),

  getById: (id) =>
    api.get(`/products/${id}`).then(r => r.data),

  getByCategory: (cat) =>
    api.get(`/products/category/${cat}`).then(r => r.data),

  search: (kw) =>
    api.get(`/products/search?keyword=${kw}`).then(r => r.data),
}

// Orders
export const orderService = {
  placeOrder: async (productQuantities) => {
    const response = await api.post('/orders/place', {
      productQuantities,
    })

    return response.data
  },

  getMyOrders: async () => {
    const response = await api.get('/orders/my')
    return response.data
  },

  confirmPayment: async (orderId, paymentData) => {
    const response = await api.post(
      `/orders/${orderId}/confirm-payment`,
      paymentData
    )

    return response.data
  },

  cancelPayment: async (orderId) => {
    const response = await api.post(
      `/orders/${orderId}/cancel-payment`
    )

    return response.data
  },

}

// Auth
export const userService = {
  register: (data) =>
    api.post('/users/register', data).then(r => r.data),

  login: (data) =>
    api.post('/users/login', data).then(r => r.data),
}

export default api