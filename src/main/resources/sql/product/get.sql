SELECT name        AS p_name,
       price       AS p_price,
       description AS p_description,
       quantity    AS p_quantity,
       image    AS p_image,
       array_remove(category_name, NULL) AS c_name
FROM (
         SELECT name,
                price,
                description,
                quantity,
                image,
                array_agg(cp.category_name) AS category_name
         FROM product
                  LEFT JOIN category_product cp ON product.name = cp.product_name
         WHERE product.name = :name
         GROUP BY name) AS select_all;