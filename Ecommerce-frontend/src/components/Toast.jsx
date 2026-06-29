import { useCart } from '../hooks/useCart'
import '../styles/global.css'

export default function Toast() {
  const { toasts } = useCart()

  if (toasts.length === 0) return null

  return (
    <div className="toast-container" role="status" aria-live="polite">
      {toasts.map(t => (
        <div key={t.id} className="toast">
          🛒 {t.msg}
        </div>
      ))}
    </div>
  )
}
