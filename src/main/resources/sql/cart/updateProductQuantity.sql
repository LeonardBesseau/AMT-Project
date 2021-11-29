UPDATE cart_product
SET quantity = :quantity
WHERE user_name = :username AND product_name = :name;