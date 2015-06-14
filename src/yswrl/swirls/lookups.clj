(ns yswrl.swirls.lookups
  (:require [yswrl.db :as db]
            [yswrl.swirls.swirl-states :as states]))
(use 'korma.core)
(use 'korma.db)


; Queries to get a single swirl with author information
(defn select-single-swirl [id]
  (-> (select* db/swirls)
      (fields :id :type :author_id :title :review :creation_date :itunes_collection_id :thumbnail_url :users.username :users.email_md5)
      (join :inner db/users (= :users.id :swirls.author_id))
      (where {:id id :state [not= states/deleted]})
      (limit 1)))

(defn get-swirl [id]
  (first (-> (select-single-swirl id)
             (select))))

(defn get-swirl-if-allowed-to-view [id user-id]
  (first (-> (select-single-swirl id)
             (where (or {:author_id user-id} {:state states/live}))
             (select))))

(defn get-swirl-if-allowed-to-edit [id user-id]
  (first (-> (select-single-swirl id)
             (where {:author_id user-id})
             (select))))


; Queries to get multiple swirls
(def multiple-live-swirls
  (-> (select* db/swirls)
      (where {:state states/live})))

(defn select-multiple-swirls [max-results skip]
  (-> multiple-live-swirls
      (fields :type :creation_date, :review, :title, :id, :users.username :users.email_md5 :thumbnail_url)
      (join :inner db/users (= :users.id :swirls.author_id))
      (offset skip)
      (limit max-results)
      (order :creation_date :desc)))

(defn get-swirls-authored-by [author-id]
  (-> (select-multiple-swirls 1000 0)
      (where {:author_id author-id})
      (select)))

(defn get-swirls-awaiting-response [user-id max-results skip]
  (-> (select-multiple-swirls max-results skip)
      (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
      (where {:suggestions.recipient_id user-id :suggestions.response_id nil})
      (select)))

(defn get-swirls-awaiting-response-count [user-id]
  (:cnt (first (-> multiple-live-swirls
                   (aggregate (count :*) :cnt)
                   (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
                   (where {:suggestions.recipient_id user-id :suggestions.response_id nil})
                   (select)))))


(defn get-swirls-by-response [user-id max-results skip response]
  (-> (select-multiple-swirls max-results skip)
      (join :inner db/swirl-responses (= :swirls.id :swirl_responses.swirl_id))
      (where {:swirl_responses.responder user-id})
      (where {(raw "LOWER(swirl_responses.summary)") (clojure.string/lower-case response)})
      (select)))

