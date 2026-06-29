import ProductCard from './ProductCard'
import SkeletonLoader from './SkeletonLoader'
import '../styles/product.css'

export default function ProductSection({ title, products, loading }) {
  // Don't render section at all if no products and not loading
  if (!loading && products.length === 0) return null

  return (
    <section className="product-section">
      <h2 className="section-title">{title}</h2>

      {loading ? (
        <SkeletonLoader count={4} />
      ) : (
        <div className="product-grid">
          {products.map(product => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </section>
  )
}
