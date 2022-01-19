SELECT product.name, product.price, product.image, cart_product.quantity
FROM product
INNER JOIN cart_product ON product.name = cart_product.product_name
WHERE cart_product.user_name = :username ORDER BY product.name;