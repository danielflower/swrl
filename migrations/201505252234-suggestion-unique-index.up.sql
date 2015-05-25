DELETE FROM suggestions
WHERE swirl_id IN (
  SELECT swirl_id
  FROM (
         SELECT
           swirl_id,
           recipient_id,
           count(1)
         FROM suggestions
         WHERE recipient_id IS NOT NULL
         GROUP BY swirl_id, recipient_id
         HAVING count(1) > 1) AS results);

DELETE FROM suggestions
WHERE swirl_id IN (
  SELECT swirl_id
  FROM (
         SELECT
           swirl_id,
           recipient_email,
           count(1)
         FROM suggestions
         WHERE recipient_email IS NOT NULL
         GROUP BY swirl_id, recipient_email
         HAVING count(1) > 1) AS results);

CREATE UNIQUE INDEX ON suggestions (swirl_id, recipient_id);
CREATE UNIQUE INDEX ON suggestions (swirl_id, recipient_email);
