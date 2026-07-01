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

  if (product.stock === 0) {
    return;
  }

  addToCart(product);
  setAdded(true);

  setTimeout(() => setAdded(false), 1200);
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
        <p
          className={`product-stock ${
            product.stock === 0
              ? "out-of-stock"
              : product.stock <= 5
              ? "low-stock"
              : "in-stock"
          }`}
        >
          {product.stock === 0
            ? "Out of Stock"
            : product.stock <= 5
            ? `Only ${product.stock} left`
            : `In Stock (${product.stock})`}
        </p>
        <button
          className={`add-btn ${added ? 'add-btn--added' : ''}`}
          onClick={handleAdd}
          disabled={product.stock === 0}
        >
          {product.stock === 0
            ? "Out of Stock"
            : added
            ? "✓ Added!"
            : "Add to Cart"}
        </button>
      </div>
    </div>
  )
}
