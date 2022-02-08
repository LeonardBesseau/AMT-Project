INSERT INTO product
VALUES ('1', 2, 'A', 10, 'e784883f-bdb1-465c-aa86-9af763406ac2'),('2', 2, 'B', 0, 'e784883f-bdb1-465c-aa86-9af763406ac2'),('3', 3, 'C', 10, 'e784883f-bdb1-465c-aa86-9af763406ac2');

INSERT INTO category
VALUES ('A'),
       ('B');

INSERT INTO category_product
VALUES ('A','1'), ('B', '1'), ('A', '2');