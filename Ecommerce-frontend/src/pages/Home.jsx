import HeroCarousel from '../components/HeroCarousel';
import ProductSection from '../components/ProductSection';
import { useProducts } from '../hooks/useProducts';

export default function Home() {
  const { trending, clothing, electronics, loading, error } = useProducts();

  return (
    <>
      <HeroCarousel />

      <div className="container">
        {error && (
          <div className="error-banner">
            ⚠️ Could not load products — make sure your Spring Boot server is running on{" "}
            <strong>localhost:8080</strong>. ({error})
          </div>
        )}

        <section id="trending">
          <ProductSection
            title="🔥 Trending Products"
            products={trending}
            loading={loading}
          />
        </section>

        <section id="clothing">
          <ProductSection
            title="👗 Clothing Collection"
            products={clothing}
            loading={loading}
          />
        </section>

        <section id="electronics">
          <ProductSection
            title="💻 Electronics & Gadgets"
            products={electronics}
            loading={loading}
          />
        </section>
      </div>
    </>
  );
}