import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useCart } from '../hooks/useCart';
import { useAuth } from '../context/AuthContext';
import '../styles/navbar.css';

export default function Navbar() {
  const { totalItems } = useCart();
  const { user, logout, isLoggedIn } = useAuth();
  const { pathname } = useLocation();
  const navigate = useNavigate();

  const scrollToSection = (sectionId) => {
  if (window.location.pathname !== "/") {
    navigate("/");

    setTimeout(() => {
      document.getElementById(sectionId)?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 150);
  } else {
    document.getElementById(sectionId)?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  }
};

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="nav-inner">
        <Link to="/" className="nav-brand">
          SHOPLANE
        </Link>

        <div className="nav-links">
          <Link
            to="/"
            className={`nav-link ${pathname === '/' ? 'active' : ''}`}
          >
            Home
          </Link>

          <button
            className="nav-link nav-button"
            onClick={() => scrollToSection("clothing")}
          >
            Clothing
          </button>

          <button
            className="nav-link nav-button"
            onClick={() => scrollToSection("electronics")}
          >
            Electronics
          </button>

          {isLoggedIn ? (
            <>
              <span className="nav-user">
                👤 {user.name}
              </span>

              <button
                className="nav-link nav-logout"
                onClick={handleLogout}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className={`nav-link ${
                  pathname === '/login' ? 'active' : ''
                }`}
              >
                Login
              </Link>

              <Link
                to="/register"
                className={`nav-link ${
                  pathname === '/register' ? 'active' : ''
                }`}
              >
                Register
              </Link>
            </>
          )}

          <Link to="/cart" className="cart-btn">
            🛒 Cart
            {totalItems > 0 && (
              <span className="cart-badge">
                {totalItems}
              </span>
            )}
          </Link>
        </div>
      </div>
    </nav>
  );
}