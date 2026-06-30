import { useAuth } from '../context/AuthContext';
import { useCart } from '../hooks/useCart'
import CartTable from '../components/CartTable'
import { orderService } from '../services/productService';
import { Link, useNavigate } from 'react-router-dom';
import '../styles/cart.css'

const formatPrice = (price) => `₹${Number(price).toLocaleString('en-IN')}`

export default function Cart() {
  const { cart, totalAmount, clearCart, showToast } = useCart()
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();


  const handleCheckout = async () => {

    if (!isLoggedIn) {
        showToast("Please login to continue to checkout.");
        navigate("/login", {
          state: {
            from: "/cart"
          }
      });
        return;
    }

    try {

        const productQuantities = {};

        cart.forEach(item => {
            productQuantities[item.id] = item.quantity;
        });

        await orderService.placeOrder(productQuantities);

        showToast("Order placed successfully. Thank you for shopping!");

        clearCart();

        navigate("/", { replace: true });
    } catch (err) {
        showToast(
          err.message || "Failed to place order."
        );
    }
  };

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
