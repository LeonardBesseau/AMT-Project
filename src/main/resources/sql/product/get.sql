SELECT name          AS p_name,
       price         AS p_price,
       description   AS p_description,
       quantity      AS p_quantity,
       image_id      AS p_image_id,
       category_name AS c_name
FROM product
         LEFT JOIN category_product cp ON product.name = cp.product_name
WHERE product.name = :name;