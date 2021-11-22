UPDATE cart_product
SET quantity = :quantity
WHERE user_id = :id AND product_name = :name;