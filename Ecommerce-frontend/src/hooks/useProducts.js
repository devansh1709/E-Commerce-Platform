import { useState, useEffect } from 'react'
import { productService } from '../services/productService'

export function useProducts() {
  const [products,  setProducts]  = useState([])
  const [loading,   setLoading]   = useState(true)
  const [error,     setError]     = useState(null)

  useEffect(() => {
    let cancelled = false

    productService.getAll()
      .then(data => { if (!cancelled) setProducts(data) })
      .catch(err  => { if (!cancelled) setError(err.message) })
      .finally(()  => { if (!cancelled) setLoading(false) })

    // Cleanup — prevents state update on unmounted component
    return () => { cancelled = true }
  }, [])

  // Pre-split by category so pages don't need to do it
  const trending    = products.filter(p =>
    p.category !== 'Mens Clothing' &&
    p.category !== 'Womens Clothing' &&
    p.category !== 'Electronics'
  )
  const clothing    = products.filter(p =>
    p.category === 'Mens Clothing' || p.category === 'Womens Clothing'
  )
  const electronics = products.filter(p => p.category === 'Electronics')

  return { products, trending, clothing, electronics, loading, error }
}
