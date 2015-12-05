UPDATE swirl_responses SET summary = 'None' WHERE summary is null;
ALTER TABLE swirl_responses ALTER COLUMN summary SET NOT NULL;