ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_username_key;
DROP INDEX IF EXISTS users_email_key;
DROP INDEX IF EXISTS users_username_key;

CREATE UNIQUE INDEX users_email_key ON users (email);
CREATE UNIQUE INDEX users_username_key ON users (username);

DROP TABLE password_reset_requests;