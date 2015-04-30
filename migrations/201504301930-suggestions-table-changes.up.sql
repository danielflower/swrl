ALTER TABLE suggestions ADD COLUMN mandrill_id VARCHAR(50) NULL;
ALTER TABLE suggestions ADD COLUMN mandrill_rejection_reason VARCHAR;

ALTER TABLE swirls DROP CONSTRAINT swirls_title_key;