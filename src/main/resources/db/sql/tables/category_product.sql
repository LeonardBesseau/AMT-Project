CREATE TABLE IF NOT EXISTS category_product
(
    category_name TEXT,
    product_name  TEXT,
    PRIMARY KEY (category_name, product_name),
    CONSTRAINT fk_category_name FOREIGN KEY (category_name) REFERENCES category (name) ON DELETE CASCADE,
    CONSTRAINT fk_product_name FOREIGN KEY (product_name) REFERENCES product (name) ON DELETE CASCADE
);