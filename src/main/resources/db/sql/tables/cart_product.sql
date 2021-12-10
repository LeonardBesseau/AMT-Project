CREATE TABLE IF NOT EXISTS cart_product
(
    user_name    TEXT,
    product_name TEXT,
    quantity     INTEGER CHECK ( quantity > 0 ),
    PRIMARY KEY (user_name, product_name),
    CONSTRAINT fk_user_name FOREIGN KEY (user_name) REFERENCES cart (user_name) ON DELETE CASCADE,
    CONSTRAINT fk_product_name FOREIGN KEY (product_name) REFERENCES product (name) ON DELETE CASCADE
);