ALTER TABLE notifications ADD COLUMN instigator_id INT NULL REFERENCES users(id);
ALTER TABLE notifications ADD COLUMN summary VARCHAR NULL;
ALTER TABLE notifications ADD COLUMN date_created TIMESTAMP NOT NULL DEFAULT now();