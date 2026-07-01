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


  // ── Toast notifications ──
  const showToast = useCallback((msg) => {
    const id = Date.now()
    setToasts(t => [...t, { id, msg }])
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 2500)
  }, [])  

  // ── Cart actions ──
  const addToCart = useCallback((product) => {

    if (product.stock === 0) {
    showToast("Product is out of stock");
    return;
  }

  setCart(prev => {

    const existing = prev.find(item => item.id === product.id);

    // Product already exists in cart
    if (existing) {

      if (existing.quantity >= product.stock) {
        showToast(`Only ${product.stock} item(s) available in stock`);
        return prev;
      }

      const updated = prev.map(item =>
        item.id === product.id
          ? { ...item, quantity: item.quantity + 1 }
          : item
      );

      localStorage.setItem("shoplane_cart", JSON.stringify(updated));
      showToast(`${product.name} added to cart`);
      return updated;
    }

    // New product
    const updated = [...prev, { ...product, quantity: 1 }];

    localStorage.setItem("shoplane_cart", JSON.stringify(updated));
    showToast(`${product.name} added to cart`);

    return updated;
  });


  }, [showToast]);

  const changeQty = useCallback((id, delta) => {

  setCart(prev => {

    const updated = prev
      .map(item => {

        if (item.id !== id) return item;

        // Prevent exceeding stock
        if (delta > 0 && item.quantity >= item.stock) {
          showToast(`Only ${item.stock} item(s) available.`);
          return item;
        }

        return {
          ...item,
          quantity: item.quantity + delta
        };

      })
      .filter(item => item.quantity > 0);

    localStorage.setItem(
      "shoplane_cart",
      JSON.stringify(updated)
    );

    return updated;

  });

  }, [showToast]);

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
