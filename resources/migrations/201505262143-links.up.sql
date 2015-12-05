
CREATE TABLE swirl_links (
  id SERIAL PRIMARY KEY,
  swirl_id INT NOT NULL REFERENCES swirls(id),
  type_code CHAR(1) NOT NULL,
  code VARCHAR NOT NULL
);

INSERT INTO swirl_links (swirl_id, type_code, code)
    SELECT id, 'I', itunes_collection_id FROM swirls WHERE itunes_collection_id IS NOT NULL;

INSERT INTO swirl_links (swirl_id, type_code, code)
  SELECT id, 'A', amazon_asin_id FROM swirls WHERE amazon_asin_id IS NOT NULL;


UPDATE swirls SET type = 'video' WHERE type = 'youtube';