ALTER TABLE users ADD COLUMN email_md5 VARCHAR(32) NULL;
UPDATE users SET email_md5 = md5(lower(email));
ALTER TABLE users ALTER COLUMN email_md5 SET NOT NULL;
