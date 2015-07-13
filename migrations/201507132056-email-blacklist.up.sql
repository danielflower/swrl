ALTER TABLE users ADD COLUMN notification_email_interval INTERVAL DEFAULT '1 week';
ALTER TABLE users ADD COLUMN inbox_email_interval INTERVAL DEFAULT '1 month';
ALTER TABLE users ADD COLUMN date_registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
CREATE TABLE email_blacklist (
  id               SERIAL PRIMARY KEY,
  email            VARCHAR   NOT NULL,
  date_blacklisted TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO email_blacklist (email) VALUES ('ali@turtle-media.com');
