import { createContext, useContext, useState, useCallback } from 'react'
import { userService } from '../services/productService'

const AuthContext = createContext(null)

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}

export function AuthProvider({ children }) {
  // Restore from localStorage on page refresh
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('shoplane_user')
      return stored ? JSON.parse(stored) : null
    } catch { return null }
  })

  const persist = (userData) => {
    if (userData) {
      localStorage.setItem('shoplane_user', JSON.stringify(userData))
      localStorage.setItem('token', userData.token)
    } else {
      localStorage.removeItem('shoplane_user')
      localStorage.removeItem('token')
    }
    setUser(userData)
  }

  const login = useCallback(async (email, password) => {
    const data = await userService.login({ email, password })
    // data = { token, userId, name, email }
    persist(data)
    return data
  }, [])

  const register = useCallback(async (name, email, password) => {
    const data = await userService.register({ name, email, password })
    persist(data)
    return data
  }, [])

  const logout = useCallback(() => {
    persist(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isLoggedIn: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}
