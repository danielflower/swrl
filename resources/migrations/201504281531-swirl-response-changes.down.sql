ALTER TABLE swirl_responses ALTER COLUMN summary DROP NOT NULL;

DROP TABLE suggestions;

DROP INDEX UQ_swirl_responses_responder;