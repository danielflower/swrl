CREATE TABLE swirl_details
(
  id              SERIAL          PRIMARY KEY,
  external_id     VARCHAR         NOT NULL,
  type            VARCHAR(8)      NOT NULL,
  details         JSONB         NULL
);

CREATE UNIQUE INDEX ON swirl_details (external_id, type);

INSERT INTO swirl_details (external_id, type)
    SELECT external_id, type from swirls where external_id is NOT NULL
    GROUP BY external_id, type;