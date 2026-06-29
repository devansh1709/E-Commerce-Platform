import '../styles/footer.css'

const FOOTER_LINKS = {
  'Online Store':   ['Men Clothing', 'Women Clothing', 'Men Accessories', 'Women Accessories'],
  'Helpful Links':  ['Home', 'About', 'Contact'],
  'Brand Partners': ['Zara', 'Pantaloons', "Levi's", 'UCB', '+ Many More'],
  'Address':        ['Building 101', 'Central Avenue', 'LA – 902722', 'United States'],
}

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-inner">
        {Object.entries(FOOTER_LINKS).map(([heading, items]) => (
          <div key={heading} className="footer-col">
            <h5>{heading}</h5>
            <ul>
              {items.map(item => <li key={item}>{item}</li>)}
            </ul>
          </div>
        ))}
      </div>
      <div className="footer-copy">
        © 2025 Shoplane | Designed by <strong>Devansh Sharma</strong>
      </div>
    </footer>
  )
}
