CREATE MATERIALIZED VIEW search_index AS
  SELECT
    s.id AS swirl_id,
    setweight(to_tsvector('english', s.title), 'A') ||
    setweight(to_tsvector('english', s.review), 'B') ||
    setweight(to_tsvector('english', coalesce(string_agg(c.html_content, ' '), '')), 'C') ||
    setweight(to_tsvector('simple', coalesce(string_agg(u.username, ' '), '')), 'D') ||
    setweight(to_tsvector('simple', coalesce(string_agg(cu.username, ' '), '')), 'D')
         AS document
  FROM swirls s
    INNER JOIN users u ON s.author_id = u.id
    LEFT JOIN comments c ON s.id = c.swirl_id
    LEFT JOIN users cu ON c.author_id = cu.id
  GROUP BY s.id;

CREATE INDEX idx_fts_search ON search_index USING gin(document);
