import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { orderService } from "../services/productService";
import "../styles/orders.css";

const formatPrice = (price) =>
  `₹${Number(price).toLocaleString("en-IN")}`;

const formatDate = (date) =>
  new Date(date).toLocaleString("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  });

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchOrders();
  }, []);

  async function fetchOrders() {
    try {
      const data = await orderService.getMyOrders();
      setOrders(data);
    } catch (err) {
      setError(err.message || "Failed to load orders.");
    } finally {
      setLoading(false);
    }
  }

  if (loading) 
    return (<div className="orders-loading"><h2>Loading orders...</h2></div>);

  if (error)
    return <h2 className="orders-error">{error}</h2>;

  if (orders.length === 0) {
    return (
      <div className="orders-empty">
        <h2>No Orders Yet</h2>
        <p>You haven't placed any orders yet.</p>

        <Link to="/" className="back-btn">
          Continue Shopping
        </Link>
      </div>
    );
  }

  return (
    <div className="orders-page">

      <h1>My Orders</h1>

      {orders.map((order) => (

        <div key={order.id} className="order-card">

          <div className="order-header">

            <h3>Order #{order.id}</h3>

            <span className={`status ${order.status.toLowerCase()}`}>
              {order.status}
            </span>

          </div>

          <p>
            <strong>Date:</strong> {formatDate(order.orderDate)}
          </p>

          <p>
            <strong>Total:</strong> {formatPrice(order.totalAmount)}
          </p>

          <table className="order-table">

            <thead>

              <tr>
                <th>Product</th>
                <th>Quantity</th>
                <th>Price</th>
              </tr>

            </thead>

            <tbody>

              {order.orderItems.map((item, index) => (

                <tr key={index}>

                  <td>{item.productName}</td>

                  <td>{item.quantity}</td>

                  <td>{formatPrice(item.productPrice)}</td>

                </tr>

              ))}

            </tbody>

          </table>

        </div>

      ))}

    </div>
  );
}