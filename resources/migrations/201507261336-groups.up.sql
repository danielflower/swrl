CREATE TABLE groups (
  id            SERIAL PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  date_created  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by_id INT          NOT NULL REFERENCES users (id),
  description   VARCHAR      NULL
);


CREATE TABLE group_members (
  id          SERIAL PRIMARY KEY,
  group_id    INT       NOT NULL REFERENCES groups (id),
  user_id     INT       NOT NULL REFERENCES users (id),
  date_joined TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE group_swirl_links (
  id          SERIAL PRIMARY KEY,
  group_id    INT       NOT NULL REFERENCES groups (id),
  swirl_id    INT       NOT NULL REFERENCES swirls (id),
  added_by_id INT       NOT NULL REFERENCES users (id),
  date_added  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE group_invites (
  id          SERIAL PRIMARY KEY,
  group_id    INT       NOT NULL REFERENCES groups (id),
  invited_by_id     INT       NOT NULL REFERENCES users (id),
  invitee_email_address VARCHAR(100) NOT NULL,
  message VARCHAR,
  date_invite_emailed TIMESTAMP NULL,
  date_joined TIMESTAMP NULL
);

CREATE UNIQUE INDEX groups_owner_name_uq ON groups(name, created_by_id);
CREATE UNIQUE INDEX group_members_uq ON group_members(group_id, user_id);
CREATE UNIQUE INDEX group_swirl_links_uq ON group_swirl_links(group_id, swirl_id, added_by_id);

-- unreleated to groups - just speeding up lookups for notifications which slows the tests
CREATE INDEX notifications_user_idx ON notifications (target_user_id);