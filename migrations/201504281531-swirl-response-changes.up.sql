UPDATE swirl_responses
SET summary = 'None'
WHERE summary IS NULL;

ALTER TABLE swirl_responses ALTER COLUMN summary SET NOT NULL;


CREATE TABLE suggestions
(
  id              SERIAL PRIMARY KEY,
  code            UUID         NOT NULL UNIQUE,
  swirl_id        INT          NOT NULL REFERENCES swirls (id),
  recipient_id    INT          NULL REFERENCES users (id),
  recipient_email VARCHAR(100) NULL,
  date_notified   TIMESTAMP    NULL,
  response_id     INT          NULL REFERENCES swirl_responses (id)
);

CREATE UNIQUE INDEX UQ_swirl_responses_responder ON swirl_responses (swirl_id, responder);