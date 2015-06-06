CREATE TABLE notifications
(
  id                SERIAL PRIMARY KEY,
  notification_type CHAR(1)   NOT NULL,
  target_user_id    INT       NOT NULL REFERENCES users (id),
  swirl_id          INT       NULL REFERENCES swirls (id),
  subject_id        INT       NULL,
  date_emailed      TIMESTAMP NULL,
  date_dismissed    TIMESTAMP NULL,
  date_seen         TIMESTAMP NULL
);

ALTER TABLE users ADD COLUMN date_last_emailed TIMESTAMP NULL;