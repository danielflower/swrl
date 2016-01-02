(ns yswrl.swirls.lookups
  (:require [yswrl.db :as db]
            [yswrl.swirls.swirl-states :as states]
            [korma.core :refer [select* limit subselect aggregate offset order where join fields select raw modifier]]))
(use 'korma.db)


; Queries to get a single swirl with author information
(defn select-single-swirl [id]
  (-> (select* db/swirls)
      (fields :id :type :author_id :title :review :creation_date :itunes_collection_id :thumbnail_url :users.username :users.email_md5 :is_private)
      (join :inner db/users (= :users.id :swirls.author_id))
      (where {:id id :state [not= states/deleted]})
      (limit 1)))

(defn get-swirl [id]
  (first (-> (select-single-swirl id)
             (select))))


(defn where-user-can-view [query requestor]
  (let [anon-allowed {:is_private false :state states/live}]
    (if (nil? requestor)
      ; for anon users, use the anon query
      (where query anon-allowed)

      ; for logged in users, one of the following:
      (where query (or anon-allowed                         ; anon users can see it, or...
                       {:author_id (requestor :id)}         ; the user is the author, or ...
                       {:is_private true                    ; it's a private swirl but the current user is one of the suggestees. or...
                        :state      states/live
                        :id         [in (subselect db/suggestions (fields :swirl_id) (where {:swirl_id :swirls.id :recipient_id (requestor :id)}))]}
                       {:is_private true                    ; it's a private swirl but it's associated with a group the user is a part of
                        :state      states/live
                        :id         [in (subselect db/group-swirl-links
                                                   (fields :group_swirl_links.swirl_id)
                                                   (join :inner db/group-members (= :group_members.group_id :group_swirl_links.group_id))
                                                   (where {:swirl_id :swirls.id :group_members.user_id (requestor :id)}))]}
                       )))
    ))

(defn get-swirl-if-allowed-to-view [id requestor]
  (first (-> (select-single-swirl id)
             (where-user-can-view requestor)
             (select))))

(defn get-swirl-if-allowed-to-edit [id user-id]
  (first (-> (select-single-swirl id)
             (where {:author_id user-id})
             (select))))


; Queries to get multiple swirls

(defn multiple-live-swirls [requestor]
  (-> (select* db/swirls)
      (where {:state states/live})                          ; this almost looks like duplication but stops a users draft and deleted swirls from showing in list views
      (where-user-can-view requestor)
      ))

(defn select-multiple-swirls [requestor max-results skip]
  (-> (multiple-live-swirls requestor)
      (fields :type :creation_date, :review, :title, :id, :users.username :users.email_md5 :thumbnail_url :author_id :is_private)
      (join :inner db/users (= :users.id :swirls.author_id))
      (offset skip)
      (limit max-results)))

(defn get-all-swirls [max-results skip requestor]
  (-> (select-multiple-swirls requestor max-results skip)
      (order :id :desc)
      (select)))

(defn search-for-swirls [max-results skip requestor search-query]
  (let [escaped-search-query (clojure.string/escape search-query {\' "''"})]
    (-> (select-multiple-swirls requestor max-results skip)
        (join :inner (raw "search_index") (= :swirls.id (raw "search_index.swirl_id")))
        (where (raw (str "search_index.document @@ plainto_tsquery('english', '" escaped-search-query "')")))
        (order (raw (str "ts_rank(search_index.document, plainto_tsquery('english', '" escaped-search-query "'))")) :desc)
        (select))))

(defn get-swirls-by-id [ids requestor]
  (-> (select-multiple-swirls requestor 100 0)
      (where {:swirls.id [in ids]})
      (order :id :desc)
      (select)))

(defn get-swirls-authored-by [author-id requestor]
  (-> (select-multiple-swirls requestor 1000 0)
      (where {:author_id author-id})
      (order :id :desc)
      (select)))

(defn get-swirls-awaiting-response [requestor max-results skip]
  (-> (select-multiple-swirls requestor max-results skip)
      (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
      (where {:suggestions.recipient_id (requestor :id) :suggestions.response_id nil})
      (order :id :desc)
      (select)))

(defn get-all-swirls-not-responded-to [max-results skip requestor]
  (-> (select-multiple-swirls requestor max-results skip)
      (where {:swirls.id [not-in (subselect db/swirl-responses (fields :swirl_id) (where {:responder (requestor :id)}))]})
      (where {:author_id [not= (requestor :id)]})
      (order :id :desc)
      (select)))

(defn get-swirls-awaiting-response-count [requestor]
  (:cnt (first (-> (multiple-live-swirls requestor)
                   (aggregate (count :*) :cnt)
                   (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
                   (where {:suggestions.recipient_id (requestor :id) :suggestions.response_id nil})
                   (select)))))


(defn get-swirls-by-response [requestor max-results skip response]
  (-> (select-multiple-swirls requestor max-results skip)
      (join :inner db/swirl-responses (= :swirls.id :swirl_responses.swirl_id))
      (where {:swirl_responses.responder (requestor :id)})
      (where {(raw "LOWER(swirl_responses.summary)") (clojure.string/lower-case response)})
      (order :id :desc)
      (select)))


(defn get-response-count-for-user [user-id]
  (db/query "SELECT r.summary, count(1) AS count FROM swirl_responses r INNER JOIN swirls s ON s.id = r.swirl_id WHERE s.state = ? AND r.responder = ? GROUP BY r.summary ORDER BY r.summary" states/live user-id))
