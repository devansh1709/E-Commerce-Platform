import { useCart } from '../hooks/useCart'
import '../styles/cart.css'

const formatPrice = (price) => `₹${Number(price).toLocaleString('en-IN')}`

export default function CartTable() {
  const { cart, changeQty, removeItem } = useCart()

  if (cart.length === 0) return null

  return (
    <div className="cart-table-wrap">
      <table className="cart-table">
        <thead>
          <tr>
            <th>Image</th>
            <th>Product</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Total</th>
            <th>Remove</th>
          </tr>
        </thead>
        <tbody>
          {cart.map(item => (
            <tr key={item.id}>
              <td>
                <img
                  className="cart-thumb"
                  src={item.imageUrl || `https://picsum.photos/seed/${item.id}/100/100`}
                  alt={item.name}
                  onError={e => { e.target.src = `https://picsum.photos/seed/item${item.id}/100/100` }}
                />
              </td>
              <td className="cart-name">{item.name}</td>
              <td>{formatPrice(item.price)}</td>
              <td>
                <div className="qty-ctrl">
                  <button className="qty-btn" onClick={() => changeQty(item.id, -1)} aria-label="Decrease">−</button>
                  <span className="qty-value">{item.quantity}</span>
                  <button className="qty-btn" onClick={() => changeQty(item.id, +1)} aria-label="Increase">+</button>
                </div>
              </td>
              <td className="cart-item-total">{formatPrice(item.price * item.quantity)}</td>
              <td>
                <button className="remove-btn" onClick={() => removeItem(item.id)} aria-label={`Remove ${item.name}`}>✕</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
