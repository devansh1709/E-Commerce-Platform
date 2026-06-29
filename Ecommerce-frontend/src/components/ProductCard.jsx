import { useState } from 'react'
import { useCart } from '../hooks/useCart'
import '../styles/product.css'

// Formats price in Indian number system: 2999 → ₹2,999
const formatPrice = (price) =>
  `₹${Number(price).toLocaleString('en-IN')}`

export default function ProductCard({ product }) {
  const { addToCart } = useCart()
  const [added, setAdded] = useState(false)

  const handleAdd = () => {
    addToCart(product)
    setAdded(true)
    setTimeout(() => setAdded(false), 1200)
  }

  const fallbackImg = `https://picsum.photos/seed/${product.id}/400/300`

  return (
    <div className="product-card">
      <div className="product-img-wrap">
        <img
          className="product-img"
          src={product.imageUrl || fallbackImg}
          alt={product.name}
          loading="lazy"
          onError={e => { e.target.src = fallbackImg }}
        />
      </div>

      <div className="product-body">
        <h3 className="product-name">{product.name}</h3>
        <p className="product-desc">{product.description}</p>
        <p className="product-price">{formatPrice(product.price)}</p>
        <button
          className={`add-btn ${added ? 'add-btn--added' : ''}`}
          onClick={handleAdd}
        >
          {added ? '✓ Added!' : 'Add to Cart'}
        </button>
      </div>
    </div>
  )
}
