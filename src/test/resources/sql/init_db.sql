CREATE TABLE IF NOT EXISTS image
(
    id   SERIAL PRIMARY KEY,
    data BYTEA
);
INSERT INTO image VALUES (0, NULL);

CREATE TABLE IF NOT EXISTS cart
(
    user_id INTEGER PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS product
(
    name        TEXT PRIMARY KEY,
    price       NUMERIC CHECK ( price >= 0 ),
    description TEXT,
    quantity    INTEGER NOT NULL CHECK ( quantity >= 0 ),
    image_id    INTEGER NOT NULL,
    CONSTRAINT fk_image_id FOREIGN KEY (image_id) REFERENCES image (id)
);

CREATE TABLE IF NOT EXISTS category
(
    name TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS category_product
(
    category_name TEXT,
    product_name  TEXT,
    PRIMARY KEY (category_name, product_name),
    CONSTRAINT fk_category_name FOREIGN KEY (category_name) REFERENCES category (name),
    CONSTRAINT fk_product_name FOREIGN KEY (product_name) REFERENCES product (name)
);



CREATE TABLE IF NOT EXISTS cart_product
(
    user_id      INTEGER,
    product_name TEXT,
    quantity     INTEGER CHECK ( quantity > 0 ),
    PRIMARY KEY (user_id, product_name),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES cart (user_id),
    CONSTRAINT fk_product_name FOREIGN KEY (product_name) REFERENCES product (name)
);