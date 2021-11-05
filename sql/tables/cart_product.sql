CREATE TABLE IF NOT EXISTS cart_product
(
    user_id      INTEGER,
    product_name TEXT,
    quantity     INTEGER CHECK ( quantity > 0 ),
    PRIMARY KEY (user_id, product_name),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES cart (user_id),
    CONSTRAINT fk_product_name FOREIGN KEY (product_name) REFERENCES product (name)
);