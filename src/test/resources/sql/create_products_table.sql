CREATE TABLE IF NOT EXISTS products
(
    name        TEXT,
    price       NUMERIC,
    description TEXT,
    ref_code    TEXT,
    tags        jsonb
);