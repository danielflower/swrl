ALTER TABLE swirls ADD COLUMN is_private BOOLEAN NULL;
UPDATE swirls SET is_private = false;
ALTER TABLE swirls ALTER COLUMN is_private SET NOT NULL;