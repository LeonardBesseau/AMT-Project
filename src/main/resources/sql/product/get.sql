SELECT name          AS p_name,
       price         AS p_price,
       description   AS p_description,
       quantity      AS p_quantity,
       image_id      AS p_image_id,
       data          AS p_image_data,
       category_name AS c_name
FROM product
         LEFT JOIN category_product cp ON product.name = cp.product_name
         INNER JOIN image i ON product.image_id = i.id
WHERE product.name = :name;