CREATE TABLE swirl_lists
(
  id             SERIAL PRIMARY KEY,
  swirl_id       INT         NOT NULL REFERENCES swirls (id),
  owner      INT         NOT NULL REFERENCES users (id),
  state        VARCHAR(50) NOT NULL DEFAULT 'wishlist',
  date_added TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO swirl_lists (swirl_id, owner, state, date_added)
    SELECT swirl_id, responder, 'wishlist' ,date_responded from swirl_responses where summary in ('Later', 'later', 'Want', 'I''ll add to my list', 'To read', 'Want to watch', 'Read this!', 'Will add to my "to watch" list', 'Looking forward to it', 'Learn!', 'Can''t wait to see', 'Argh not on PlayStation Now yet!', 'learn', 'Want to read it!', 'Movie watch list', 'Book list', 'Pending');

INSERT INTO swirl_lists (swirl_id, owner, state, date_added)
    SELECT swirl_id, responder, 'consuming' ,date_responded from swirl_responses where summary in ('Subscribed', 'Reading', 'Watching', 'Purchased');

INSERT INTO swirl_lists (swirl_id, owner, state, date_added)
    SELECT swirl_id, responder, 'done' ,date_responded from swirl_responses where summary not in ('Later', 'later', 'Want', 'I''ll add to my list', 'To read', 'Want to watch', 'Read this!', 'Will add to my "to watch" list', 'Looking forward to it', 'Learn!', 'Can''t wait to see', 'Argh not on PlayStation Now yet!', 'learn', 'Want to read it!', 'Movie watch list', 'Book list', 'Subscribed', 'Reading', 'Watching', 'Purchased', 'Pending', 'Not for me', 'Dismissed');
