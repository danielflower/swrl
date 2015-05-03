ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_username_key;
DROP INDEX IF EXISTS users_email_key;
DROP INDEX IF EXISTS users_username_key;

CREATE UNIQUE INDEX users_email_key ON users (lower(email));
CREATE UNIQUE INDEX users_username_key ON users (lower(username));

CREATE TABLE password_reset_requests
(
  hashed_token VARCHAR PRIMARY KEY,
  user_id INT NOT NULL REFERENCES users(id),
  date_requested TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);