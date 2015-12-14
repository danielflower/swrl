
CREATE TABLE network_connections
(
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES users(id),
  relation_type VARCHAR NOT NULL,
  another_user_id INT NOT NULL CHECK (another_user_id != user_id) REFERENCES users(id),
  date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX network_connections_uq ON network_connections(user_id, another_user_id);
