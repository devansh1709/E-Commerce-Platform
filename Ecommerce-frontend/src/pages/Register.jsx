import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/auth.css';
import { FaEye, FaEyeSlash } from "react-icons/fa";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const passwordRegex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;

  const handleChange = (e) => {
    setError(null);

    const value =
        e.target.name === "name"
            ? e.target.value.trimStart()
            : e.target.value;

    setForm((prev) => ({
        ...prev,
        [e.target.name]: value
    }));
};

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!passwordRegex.test(form.password)) {
      setError("Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number, and one special character.");
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);

    try {
      await register(
        form.name.trim(),
        form.email.trim().toLowerCase(),
        form.password
      );

      navigate('/');
    } catch (err) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">

        <h2 className="auth-title">Create Account</h2>

        <p className="auth-sub">
          Join Shoplane and start shopping
        </p>

        {error && (
          <div className="auth-error">
            {error}
          </div>
        )}

        <form
          onSubmit={handleSubmit}
          className="auth-form"
        >

          <div className="auth-field">
            <label>Full Name</label>

            <input
              type="text"
              name="name"
              autoComplete="name"
              disabled={loading}
              value={form.name}
              onChange={handleChange}
              placeholder="Devansh Gupta"
              required
            />
          </div>

          <div className="auth-field">
            <label>Email</label>

            <input
              type="email"
              name="email"
              autoComplete="email"
              disabled={loading}
              value={form.email}
              onChange={handleChange}
              placeholder="you@example.com"
              required
            />
          </div>

          <div className="auth-field">
            <label>Password</label>

            <div className="password-wrapper">
              <input
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  disabled={loading}
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="Minimum 8 characters"
                required
              />

              <button
                type="button"
                className="toggle-password"
                disabled={loading}
                onClick={() =>
                  setShowPassword(!showPassword)
                }
              >
                {showPassword ? <FaEyeSlash /> : <FaEye />}
              </button>
            </div>
          </div>

          <div className="auth-field">
            <label>Confirm Password</label>

            <div className="password-wrapper">
              <input
                type={
                    showConfirmPassword
                        ? 'text'
                        : 'password'
                }
                autoComplete="new-password"
                disabled={loading}
                name="confirmPassword"
                value={form.confirmPassword}
                onChange={handleChange}
                placeholder="Re-enter password"
                required
              />

              <button
                type="button"
                className="toggle-password"
                disabled={loading}
                onClick={() =>
                  setShowConfirmPassword(
                    !showConfirmPassword
                  )
                }
              >
                {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
              </button>
            </div>
          </div>

          {form.password && (
              <small
                  className={
                      form.password.length >= 12
                          ? "password-match"
                          : form.password.length >= 8
                          ? "password-warning"
                          : "password-mismatch"
                  }
              >
                  {form.password.length >= 12
                      ? "Strong Password"
                      : form.password.length >= 8
                      ? "Medium Password"
                      : "Weak Password"}
              </small>
          )}

          {form.confirmPassword && (
            <small
              className={
                form.password === form.confirmPassword
                  ? "password-match"
                  : "password-mismatch"
              }
            >
              {form.password ===
              form.confirmPassword
                ? '✓ Passwords match'
                : 'Passwords do not match'}
            </small>
          )}

          <button
            type="submit"
            className="auth-btn"
            disabled={
              loading ||
              !passwordRegex.test(form.password) ||
              form.password !==
                form.confirmPassword
            }
          >
            {loading
                ? 'Creating your account...'
                : 'Create Account'}
          </button>

        </form>

        <p className="auth-switch">
          Already have an account?{" "}
          {loading ? (
            <span className="auth-link-disabled">Sign In</span>
          ) : (
            <Link to="/login">Sign In</Link>
          )}
        </p>

      </div>
    </div>
  );
}