INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Wireless Headphones',
    'Premium noise-cancelling over-ear headphones with 30hr battery life',
    2999.00, 10,
    'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600',
    'Electronics', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Wireless Headphones'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Mechanical Keyboard',
    'TKL RGB mechanical keyboard with Cherry MX switches',
    3499.00, 10,
    'https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=600',
    'Electronics', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Mechanical Keyboard'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'USB-C Hub',
    '7-in-1 USB-C hub with HDMI, SD card, and 3x USB 3.0',
    1299.00, 10,
    'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600',
    'Electronics', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'USB-C Hub'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Smart Watch',
    'Fitness tracker with heart rate monitor and sleep tracking',
    4999.00, 10,
    'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600',
    'Electronics', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Smart Watch'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Classic White T-Shirt',
    '100% cotton crew-neck tee, available in S/M/L/XL',
    499.00, 10,
    'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600',
    'Mens Clothing', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Classic White T-Shirt'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Slim Fit Chinos',
    'Stretch cotton slim-fit chinos in navy blue',
    1199.00, 10,
    'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600',
    'Mens Clothing', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Slim Fit Chinos'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Denim Jacket',
    'Classic denim jacket with button closure and chest pockets',
    1799.00, 10,
    'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600',
    'Mens Clothing', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Denim Jacket'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Floral Kurta',
    'Cotton printed kurta with three-quarter sleeves',
    799.00, 10,
    'https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=600',
    'Womens Clothing', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Floral Kurta'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Palazzo Pants',
    'Wide-leg palazzo pants in georgette fabric',
    999.00, 10,
    'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600',
    'Womens Clothing', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Palazzo Pants'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Embroidered Dupatta',
    'Sheer chiffon dupatta with embroidered border',
    449.00, 10,
    'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=600',
    'Womens Clothing', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Embroidered Dupatta'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Gold Hoop Earrings',
    '18K gold-plated brass hoop earrings, 30mm diameter',
    699.00, 10,
    'https://images.unsplash.com/photo-1617038220319-276d3cfab638?w=600',
    'Jewellery', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Gold Hoop Earrings'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Silver Bracelet',
    'Sterling silver link bracelet with lobster clasp',
    899.00, 10,
    'https://images.unsplash.com/photo-1611591437281-460bfbe1220a?w=600',
    'Jewellery', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Silver Bracelet'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Pearl Necklace',
    'Freshwater pearl strand necklace with silver clasp',
    1599.00, 10,
    'https://images.unsplash.com/photo-1617038260897-41a1f14a8ca0?w=600',
    'Jewellery', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Pearl Necklace'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Effective Java 3rd Edition',
    'Best practices for the Java platform by Joshua Bloch',
    799.00, 10,
    'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600',
    'Books', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Effective Java 3rd Edition'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Clean Code',
    'A handbook of agile software craftsmanship by Robert C. Martin',
    699.00, 10,
    'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600',
    'Books', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Clean Code'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'System Design Interview',
    'An insider''s guide Vol 1 by Alex Xu',
    899.00, 10,
    'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=600',
    'Books', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'System Design Interview'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Spring Boot in Action',
    'Covers Spring Boot 3.x with real-world examples',
    749.00, 10,
    'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600',
    'Books', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Spring Boot in Action'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Designing Data-Intensive Applications',
    'The big ideas behind reliable, scalable, and maintainable systems',
    999.00, 10,
    'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=600',
    'Books', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Designing Data-Intensive Applications'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'Head First Design Patterns',
    'Object-oriented design patterns with Java examples',
    849.00, 10,
    'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600',
    'Books', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'Head First Design Patterns'
);

INSERT INTO product
(name, description, price, stock, image_url, category, version)
SELECT
    'The Pragmatic Programmer',
    'Your journey to mastery, 20th Anniversary Edition',
    799.00, 10,
    'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=600',
    'Books', 0
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE name = 'The Pragmatic Programmer'
);