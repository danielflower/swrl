CREATE TABLE swirl_weightings
(
  id                                        SERIAL      PRIMARY KEY,
  swirl_id                                  INT         NOT NULL REFERENCES swirls (id),
  user_id                                   INT         NOT NULL REFERENCES users (id),
  updated                                   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created                                   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_author                                 BOOLEAN     NOT NULL DEFAULT false,
  is_recipient                              BOOLEAN     NOT NULL DEFAULT false,
  author_is_friend                          BOOLEAN     NOT NULL DEFAULT false,
  has_responded                             BOOLEAN     NOT NULL DEFAULT false,
  list_state                                VARCHAR(50) NULL,
  number_of_comments                        INT         NOT NULL DEFAULT 0,
  number_of_comments_from_friends           INT         NOT NULL DEFAULT 0,
  number_of_positive_responses              INT         NOT NULL DEFAULT 0,
  number_of_positive_responses_from_friends INT         NOT NULL DEFAULT 0
);


CREATE UNIQUE INDEX ON swirl_weightings (swirl_id, user_id);

-- update the swirl_lists table to include the dismissed state
INSERT INTO swirl_lists (swirl_id, owner, state, date_added)
    SELECT swirl_id, responder, 'dismissed' ,date_responded from swirl_responses where summary = 'Dismissed';


-- create the positive_responses table
CREATE TABLE positive_responses
(
  id SERIAL PRIMARY KEY,
  summary VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO positive_responses (summary)
VALUES
('Nice one'),
('Funny'),
('Boosh'),
('Ha'),
('Want'),
('interesting'),
('It sure is'),
('Like'),
('Nice'),
('Agreed'),
('Want to watch'),
('Pretty cool'),
('Nice!'),
('Haha...'),
('Read this!'),
('Hodor; hodor!'),
('Reading'),
('Ok'),
('I liked it'),
('like'),
('Looking forward to it'),
('Learn!'),
('Can''t wait to see'),
('Unbelievable'),
('very good!'),
('learn'),
('Watching'),
('Loved it'),
('glol++'),
('Interesting'),
('Liked it'),
('Purchased'),
('crazy!'),
('Awesome'),
('funny'),
('beauty tail recursive'),
('Book list'),
('funny!'),
('6/10');



-- initial population of swirl_weightings
INSERT INTO swirl_weightings (swirl_id, user_id, updated, created,is_author, is_recipient, has_responded,list_state,author_is_friend,
                             number_of_comments,number_of_comments_from_friends,number_of_positive_responses,number_of_positive_responses_from_friends)
select s.id as swirl_id, u.id as user_id, s.creation_date AS updated,
s.creation_date as created,

(s.author_id = u.id) AS is_author,

EXISTS(SELECT 1 FROM suggestions sug WHERE sug.swirl_id=s.id
       AND sug.recipient_id=u.id) AS is_recipient,

EXISTS(SELECT 1 FROM swirl_responses resp WHERE resp.swirl_id=s.id
       AND resp.responder=u.id) AS has_responded,

(SELECT lists.state FROM swirl_lists lists WHERE lists.swirl_id=s.id
 AND lists.owner=u.id) AS list_state,

EXISTS(SELECT 1 FROM network_connections net WHERE net.user_id=u.id
       AND net.another_user_id=s.author_id AND relation_type='knows')
       AS author_is_friend,

(SELECT COUNT(1) FROM comments c where c.swirl_id = s.id)
AS number_of_comments,

(SELECT COUNT(1) FROM comments c2 where c2.swirl_id = s.id and
c2.author_id in (SELECT another_user_id from network_connections
                 where
                 user_id=u.id and relation_type='knows'))
AS number_of_comments_from_friends,

(SELECT COUNT(1) FROM swirl_responses resp2 where resp2.swirl_id = s.id and
     resp2.summary in (SELECT summary from positive_responses))
AS number_of_positive_responses,

(SELECT COUNT(1) FROM swirl_responses resp3 where resp3.swirl_id = s.id and
     resp3.summary in (SELECT summary from positive_responses) and
     resp3.responder in (SELECT another_user_id from network_connections
                         where
                         user_id=u.id and relation_type='knows'))
AS number_of_positive_responses_from_friends
from users u
cross join swirls s;
