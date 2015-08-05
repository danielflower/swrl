
ALTER TABLE groups ADD COLUMN join_code INTEGER NULL;
UPDATE groups SET join_code = (id * 123) / 8;
ALTER TABLE groups ALTER COLUMN join_code SET NOT NULL;
