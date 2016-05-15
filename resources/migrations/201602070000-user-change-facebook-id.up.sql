ALTER TABLE users ADD COLUMN facebook_id BIGINT NULL;

ALTER TABLE users ADD COLUMN avatar_type VARCHAR(8) NULL;

UPDATE users
SET avatar_type = 'gravatar'
WHERE avatar_type IS NULL;

ALTER TABLE users ALTER COLUMN avatar_type SET NOT NULL;
