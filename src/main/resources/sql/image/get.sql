SELECT id   AS id,
       data AS data
FROM image
WHERE image.id = :id;