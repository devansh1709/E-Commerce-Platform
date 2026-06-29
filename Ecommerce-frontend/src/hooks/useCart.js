// Re-export from CartContext so components can import from either:
//   import { useCart } from '../hooks/useCart'     ← clean component import
//   import { useCart } from '../context/CartContext' ← direct context import
//
// Both work — the hook file keeps component imports tidy and
// follows the hooks/ convention interviewers expect to see.

export { useCart } from '../context/CartContext'
