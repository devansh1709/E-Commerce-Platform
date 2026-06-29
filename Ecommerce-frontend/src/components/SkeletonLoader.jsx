import '../styles/product.css'

export default function SkeletonLoader({ count = 4 }) {
  return (
    <div className="product-grid">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="skeleton-card" aria-hidden="true">
          <div className="skeleton-img" />
          <div className="skeleton-body">
            <div className="skeleton-line skeleton-line--title" />
            <div className="skeleton-line" />
            <div className="skeleton-line skeleton-line--short" />
            <div className="skeleton-line skeleton-line--btn" />
          </div>
        </div>
      ))}
    </div>
  )
}
