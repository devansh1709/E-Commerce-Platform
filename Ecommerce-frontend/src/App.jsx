import { Routes, Route } from 'react-router-dom'
import { CartProvider } from './context/CartContext'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import Toast from './components/Toast'
import Home from './pages/Home'
import Cart from './pages/Cart'

export default function App() {
  return (
    <CartProvider>
      <Navbar />
      <main className="page-wrapper">
        <Routes>
          <Route path="/"     element={<Home />} />
          <Route path="/cart" element={<Cart />} />
        </Routes>
      </main>
      <Footer />
      <Toast />
    </CartProvider>
  )
}
