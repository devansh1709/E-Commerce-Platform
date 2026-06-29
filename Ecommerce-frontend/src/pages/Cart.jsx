import { Link } from 'react-router-dom'
import { useCart } from '../hooks/useCart'
import CartTable from '../components/CartTable'
import '../styles/cart.css'

const formatPrice = (price) => `₹${Number(price).toLocaleString('en-IN')}`

export default function Cart() {
  const { cart, totalAmount, clearCart } = useCart()

  const handleCheckout = () => {
    // TODO: integrate payment gateway (Razorpay / Stripe)
    alert('Proceeding to payment...')
  }

  // Empty cart state
  if (cart.length === 0) {
    return (
      <div className="cart-page">
        <h2 className="cart-heading">Your Shopping Cart</h2>
        <div className="empty-cart">
          <div className="empty-cart__icon">🛒</div>
          <p className="empty-cart__msg">Your cart is empty</p>
          <Link to="/" className="back-btn">← Continue Shopping</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="cart-page">
      <div className="cart-header">
        <h2 className="cart-heading">Your Shopping Cart</h2>
        <button className="clear-btn" onClick={clearCart}>Clear Cart</button>
      </div>

      <CartTable />

      <div className="cart-summary">
        <div className="cart-total">
          Grand Total: <span>{formatPrice(totalAmount)}</span>
        </div>
        <div className="cart-actions">
          <Link to="/" className="back-btn">← Continue Shopping</Link>
          <button className="checkout-btn" onClick={handleCheckout}>
            Proceed to Payment →
          </button>
        </div>
      </div>
    </div>
  )
}
