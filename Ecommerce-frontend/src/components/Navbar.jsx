import { Link, useLocation } from 'react-router-dom'
import { useCart } from '../hooks/useCart'
import '../styles/navbar.css'

export default function Navbar() {
  const { totalItems } = useCart()
  const { pathname }   = useLocation()

  return (
    <nav className="navbar">
      <div className="nav-inner">
        <Link to="/" className="nav-brand">SHOPLANE</Link>

        <div className="nav-links">
          <Link to="/"     className={`nav-link ${pathname === '/'     ? 'active' : ''}`}>Home</Link>
          <span className="nav-link">Clothing</span>
          <span className="nav-link">Accessories</span>

          <Link to="/cart" className="cart-btn">
            🛒 Cart
            {totalItems > 0 && (
              <span className="cart-badge">{totalItems}</span>
            )}
          </Link>
        </div>
      </div>
    </nav>
  )
}
