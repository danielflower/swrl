CREATE TABLE comments
(
  id             SERIAL PRIMARY KEY,
  swirl_id       INT       NOT NULL REFERENCES swirls (id),
  author_id      INT       NOT NULL REFERENCES users (id),
  html_content   TEXT      NOT NULL,
  date_responded TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO comments (swirl_id, author_id, html_content, date_responded)
    SELECT swirl_id, responder, full_response, date_responded FROM swirl_responses WHERE full_response IS NOT NULL;

ALTER TABLE swirl_responses DROP COLUMN full_response;
