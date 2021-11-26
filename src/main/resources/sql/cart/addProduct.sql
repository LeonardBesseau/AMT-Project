INSERT INTO cart_product
VALUES (:username, :name, :quantity)
ON CONFLICT (user_name, product_name) DO UPDATE SET quantity=:quantity + cart_product.quantity