INSERT INTO category_product
VALUES (:category_name, :product_name)
ON CONFLICT DO NOTHING;