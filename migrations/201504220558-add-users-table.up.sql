CREATE TABLE users
(
  id         SERIAL PRIMARY KEY,
  username   VARCHAR(50)  NOT NULL UNIQUE,
  email      VARCHAR(100) NOT NULL,
  admin      BOOLEAN      NOT NULL,
  last_login TIME         NULL,
  is_active  BOOLEAN      NOT NULL,
  password   VARCHAR      NOT NULL
);
