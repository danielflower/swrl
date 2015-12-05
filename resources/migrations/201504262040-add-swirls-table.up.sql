CREATE TABLE swirls
(
  id            SERIAL PRIMARY KEY,
  author_id     INT          NOT NULL REFERENCES users (id),
  title         VARCHAR(200) NOT NULL UNIQUE,
  review        TEXT         NOT NULL,
  creation_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE swirl_responses
(
  id             SERIAL PRIMARY KEY,
  swirl_id       INT         NOT NULL REFERENCES swirls (id),
  responder      INT         NOT NULL REFERENCES users (id),
  summary        VARCHAR(50) NOT NULL,
  full_response  TEXT        NULL,
  date_responded TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);