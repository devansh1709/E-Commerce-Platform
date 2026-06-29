import { useState, useEffect } from 'react'
import '../styles/hero.css'

const SLIDES = [
  {
    gradient: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
    title: 'PUMA Flat 30% Off',
    subtitle: 'Upgrade your wardrobe with the latest collection',
    emoji: '👟',
  },
  {
    gradient: 'linear-gradient(135deg, #0d0d0d 0%, #1a0a00 50%, #2d1400 100%)',
    title: 'Exclusive Footwear Collection',
    subtitle: 'Get the best deals on top brands',
    emoji: '👠',
  },
  {
    gradient: 'linear-gradient(135deg, #0a0a1a 0%, #1a1a0a 50%, #2a2a00 100%)',
    title: 'Luxury Watches Collection',
    subtitle: 'Stylish and premium designs for all occasions',
    emoji: '⌚',
  },
]

export default function HeroCarousel() {
  const [current, setCurrent] = useState(0)

  // Auto-advance every 4 seconds
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrent(c => (c + 1) % SLIDES.length)
    }, 4000)
    return () => clearInterval(timer)
  }, [])

  const prev = () => setCurrent(c => (c - 1 + SLIDES.length) % SLIDES.length)
  const next = () => setCurrent(c => (c + 1) % SLIDES.length)

  return (
    <div className="hero">
      {SLIDES.map((slide, i) => (
        <div
          key={i}
          className={`hero-slide ${i === current ? 'active' : 'inactive'}`}
          style={{ background: slide.gradient }}
        >
          <div className="hero-content">
            <span className="hero-emoji">{slide.emoji}</span>
            <h2 className="hero-title">{slide.title}</h2>
            <p className="hero-subtitle">{slide.subtitle}</p>
            <button className="hero-btn">Shop Now</button>
          </div>
        </div>
      ))}

      <button className="hero-arrow hero-arrow--left"  onClick={prev} aria-label="Previous">‹</button>
      <button className="hero-arrow hero-arrow--right" onClick={next} aria-label="Next">›</button>

      <div className="hero-dots" role="tablist">
        {SLIDES.map((_, i) => (
          <button
            key={i}
            role="tab"
            aria-selected={i === current}
            className={`hero-dot ${i === current ? 'active' : ''}`}
            onClick={() => setCurrent(i)}
            aria-label={`Slide ${i + 1}`}
          />
        ))}
      </div>
    </div>
  )
}
