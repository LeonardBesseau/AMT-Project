CREATE TABLE IF NOT EXISTS product
(
    name        TEXT PRIMARY KEY,
    price       NUMERIC CHECK ( price >= 0 ),
    description TEXT,
    quantity    INTEGER NOT NULL CHECK ( quantity >= 0 ),
    image_id    INTEGER NOT NULL,
    CONSTRAINT fk_image_id FOREIGN KEY (image_id) REFERENCES image (id)
);