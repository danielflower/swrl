(ns yswrl.swirls.lookups
  (:require [yswrl.db :as db]
            [yswrl.swirls.swirl-states :as states]))
(use 'korma.core)
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
      (where query anon-allowed)
      (where query (or anon-allowed
                       {:author_id (requestor :id)}
                       {:is_private true :state states/live :id [in

                                                                 ;(union (queries
                                                                          (subselect db/suggestions (fields :swirl_id) (where {:recipient_id (requestor :id)}))
                                                                          ;(subselect db/group-swirl-links
                                                                          ;           (fields :group-swirl-links.swirl_id)
                                                                          ;           (join :inner db/group-members (= :group-members.group_id :group-swirl-links.group_id))
                                                                          ;           (where {:group-members.user_id (requestor :id)}))
                                                                          ;))

                                                                 ]}


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
      (where {:state states/live}) ; this almost looks like duplication but stops a users draft and deleted swirls from showing in list views
      (where-user-can-view requestor)
      ))

(defn select-multiple-swirls [requestor max-results skip]
  (-> (multiple-live-swirls requestor)
      (fields :type :creation_date, :review, :title, :id, :users.username :users.email_md5 :thumbnail_url :author_id :is_private)
      (join :inner db/users (= :users.id :swirls.author_id))
      (offset skip)
      (limit max-results)
      (order :id :desc)))                                   ; faster to order by ID rather than creation date as ID is indexed

(defn get-all-swirls [max-results skip requestor]
  (-> (select-multiple-swirls requestor max-results skip)
      (select)))

(defn get-swirls-authored-by [author-id requestor]
  (-> (select-multiple-swirls requestor 1000 0)
      (where {:author_id author-id})
      (select)))

(defn get-swirls-awaiting-response [requestor max-results skip]
  (-> (select-multiple-swirls requestor max-results skip)
      (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
      (where {:suggestions.recipient_id (requestor :id) :suggestions.response_id nil})
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
      (select)))


(defn get-response-count-for-user [user-id]
  (db/query "SELECT r.summary, count(1) AS count FROM swirl_responses r INNER JOIN swirls s ON s.id = r.swirl_id WHERE s.state = ? AND r.responder = ? GROUP BY r.summary ORDER BY r.summary" states/live user-id))
