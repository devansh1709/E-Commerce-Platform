import { createContext, useContext, useState, useCallback } from 'react'

// 1. Create the context
const CartContext = createContext(null)

// 2. Custom hook — any component calls useCart() to access cart
export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used inside <CartProvider>')
  return ctx
}

// 3. Provider wraps the whole app (see App.jsx)
export function CartProvider({ children }) {
  const [cart, setCart] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('shoplane_cart')) || []
    } catch {
      return []
    }
  })

  const [toasts, setToasts] = useState([])

  // ── Persist to localStorage on every change ──
  const persist = useCallback((updated) => {
    setCart(updated)
    localStorage.setItem('shoplane_cart', JSON.stringify(updated))
  }, [])

  // ── Cart actions ──
  const addToCart = useCallback((product) => {
    setCart(prev => {
      const idx = prev.findIndex(i => i.id === product.id)
      const updated = idx !== -1
        ? prev.map((i, n) => n === idx ? { ...i, quantity: i.quantity + 1 } : i)
        : [...prev, { ...product, quantity: 1 }]
      localStorage.setItem('shoplane_cart', JSON.stringify(updated))
      return updated
    })
    showToast(`${product.name} added to cart`)
  }, [])

  const changeQty = useCallback((id, delta) => {
    setCart(prev => {
      const updated = prev
        .map(i => i.id === id ? { ...i, quantity: i.quantity + delta } : i)
        .filter(i => i.quantity > 0)
      localStorage.setItem('shoplane_cart', JSON.stringify(updated))
      return updated
    })
  }, [])

  const removeItem = useCallback((id) => {
    setCart(prev => {
      const updated = prev.filter(i => i.id !== id)
      localStorage.setItem('shoplane_cart', JSON.stringify(updated))
      return updated
    })
  }, [])

  const clearCart = useCallback(() => {
    setCart([])
    localStorage.removeItem('shoplane_cart')
  }, [])

  // ── Toast notifications ──
  const showToast = useCallback((msg) => {
    const id = Date.now()
    setToasts(t => [...t, { id, msg }])
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 2500)
  }, [])

  // ── Derived values ──
  const totalItems  = cart.reduce((s, i) => s + i.quantity, 0)
  const totalAmount = cart.reduce((s, i) => s + i.price * i.quantity, 0)

  return (
    <CartContext.Provider value={{
    cart,
    addToCart,
    changeQty,
    removeItem,
    clearCart,
    totalItems,
    totalAmount,
    toasts,
    showToast,
    }}>
      {children}
    </CartContext.Provider>
  )
}
