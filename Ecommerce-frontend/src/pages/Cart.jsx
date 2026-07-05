import { useAuth } from '../context/AuthContext'
import { useCart } from '../hooks/useCart'
import CartTable from '../components/CartTable'
import { Link, useNavigate } from 'react-router-dom'
import '../styles/cart.css'
import {
  productService,
  orderService
} from '../services/productService'

const formatPrice = (price) =>
  `₹${Number(price).toLocaleString('en-IN')}`

function loadRazorpayScript() {
  return new Promise((resolve) => {
    const existingScript = document.querySelector(
      'script[src="https://checkout.razorpay.com/v1/checkout.js"]'
    )

    if (existingScript) {
      resolve(true)
      return
    }

    const script = document.createElement('script')
    script.src =
      'https://checkout.razorpay.com/v1/checkout.js'

    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)

    document.body.appendChild(script)
  })
}

export default function Cart() {
  const {
    cart,
    totalAmount,
    clearCart,
    showToast
  } = useCart()

  const { isLoggedIn, user } = useAuth()
  const navigate = useNavigate()

  const handleCheckout = async () => {
    if (!isLoggedIn) {
      showToast('Please login to continue to checkout.')

      navigate('/login', {
        state: {
          from: '/cart'
        }
      })

      return
    }

    try {
      const productQuantities = {}

      cart.forEach((item) => {
        productQuantities[item.id] = item.quantity
      })

      // Check latest stock before creating an order
      for (const item of cart) {
        const latestProduct =
          await productService.getById(item.id)

        if (item.quantity > latestProduct.stock) {
          showToast(
            `${item.name} now has only ${latestProduct.stock} item(s) available.`
          )

          return
        }
      }

      const scriptLoaded =
        await loadRazorpayScript()

      if (!scriptLoaded) {
        showToast(
          'Razorpay checkout could not be loaded.'
        )
        return
      }

      const order =
        await orderService.placeOrder(
          productQuantities
        )

      if (!order.razorpayOrderId) {
        showToast(
          'Payment order could not be created.'
        )
        return
      }

      let paymentCompleted = false

      const options = {
        key: import.meta.env.VITE_RAZORPAY_KEY_ID,

        amount:
          Math.round(
            Number(order.totalAmount) * 100
          ),

        currency: 'INR',
        name: 'Shoplane',

        description:
          `Payment for order #${order.id}`,

        order_id: order.razorpayOrderId,

        handler: async function (response) {
          // Prevent ondismiss from cancelling
          // an already successful payment
          paymentCompleted = true

          try {
            await orderService.confirmPayment(
              order.id,
              {
                razorpayOrderId:
                  response.razorpay_order_id,

                razorpayPaymentId:
                  response.razorpay_payment_id,

                razorpaySignature:
                  response.razorpay_signature
              }
            )

            showToast(
              'Payment successful. Order confirmed!'
            )

            clearCart()
            navigate('/', { replace: true })

          } catch (error) {
            console.error(
              'Payment confirmation failed:',
              error
            )

            showToast(
              'Payment completed, but order confirmation failed.'
            )
          }
        },

        modal: {
          ondismiss: async function () {
            if (paymentCompleted) {
              return
            }

            try {
              await orderService.cancelPayment(
                order.id
              )

              showToast(
                'Payment cancelled. Reserved stock has been restored.'
              )

            } catch (error) {
              console.error(
                'Could not cancel pending order:',
                error
              )
            }
          }
        },

        prefill: {
          name: user?.name || '',
          email: user?.email || ''
        },

        theme: {
          color: '#3399cc'
        }
      }

      const razorpay =
        new window.Razorpay(options)

      razorpay.open()

    } catch (err) {
      showToast(
        err.message || 'Failed to place order.'
      )
    }
  }

  if (cart.length === 0) {
    return (
      <div className="cart-page">
        <h2 className="cart-heading">
          Your Shopping Cart
        </h2>

        <div className="empty-cart">
          <div className="empty-cart__icon">
            🛒
          </div>

          <p className="empty-cart__msg">
            Your cart is empty
          </p>

          <Link to="/" className="back-btn">
            ← Continue Shopping
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="cart-page">
      <div className="cart-header">
        <h2 className="cart-heading">
          Your Shopping Cart
        </h2>

        <button
          className="clear-btn"
          onClick={clearCart}
        >
          Clear Cart
        </button>
      </div>

      <CartTable />

      <div className="cart-summary">
        <div className="cart-total">
          Grand Total:{' '}
          <span>
            {formatPrice(totalAmount)}
          </span>
        </div>

        <div className="cart-actions">
          <Link to="/" className="back-btn">
            ← Continue Shopping
          </Link>

          <button
            className="checkout-btn"
            onClick={handleCheckout}
          >
            Proceed to Payment →
          </button>
        </div>
      </div>
    </div>
  )
}