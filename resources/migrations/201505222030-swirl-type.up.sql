ALTER TABLE swirls ADD COLUMN amazon_asin_id VARCHAR(20) NULL;


ALTER TABLE swirls ADD COLUMN type VARCHAR(8) NULL;
UPDATE swirls SET type = 'youtube' where review like '%youtube%';
UPDATE swirls SET type = 'album' where itunes_collection_id is not null;
UPDATE swirls SET type = 'book' where review like '%amazon%';
UPDATE swirls SET type = 'website' where type is null;
ALTER TABLE swirls ALTER COLUMN type SET NOT NULL;
