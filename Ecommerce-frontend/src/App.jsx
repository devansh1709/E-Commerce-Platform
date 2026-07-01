import { Routes, Route } from 'react-router-dom';
import { CartProvider } from './context/CartContext';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Toast from './components/Toast';
import Home from './pages/Home';
import Cart from './pages/Cart';
import Login from './pages/Login';
import Register from './pages/Register';
import RequireAdmin from "./components/RequireAdmin";
import Admin from "./pages/Admin";
import Orders from "./pages/Orders";
import RequireAuth from "./components/RequireAuth";

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Navbar />

        <main className="page-wrapper">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/admin" element={<RequireAdmin><Admin /></RequireAdmin> } />
            <Route path="/orders" element={<RequireAuth><Orders /></RequireAuth>}/>
          </Routes>
        </main>

        <Footer />
        <Toast />
      </CartProvider>
    </AuthProvider>
  );
}