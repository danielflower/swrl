(ns yswrl.swirls.swirls-repo
  (:require [yswrl.db :as db]
            [yswrl.user.networking :as networking]
            [yswrl.auth.auth-repo :as auth]
            [clojure.tools.logging :as log])
  (:import (org.postgresql.util PSQLException)))
(use 'korma.core)
(use 'korma.db)

(defn uuid [] (java.util.UUID/randomUUID))
(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))
(defn not-in? [seq elm] (not (in? seq elm)))

(defn not-found-names [found-users all-names]
  (let [found-user-names (concat (map (fn [user] (:username user)) found-users) (map (fn [user] (:email user)) found-users))]
    (filter (fn [name] (not-in? found-user-names name)) all-names)))

(defn get-suggestion [code]
  (first (select db/suggestions
                 (fields :recipient_id :recipient_email)
                 (where {:code code}))))

(defn create-suggestions [recipientUserIdsOrEmailAddresses swirlId]
  (let [found-users (auth/get-users-by-username_or_email (distinct recipientUserIdsOrEmailAddresses))]
    (->> found-users
         (map (fn [user] {:recipient_id (:id user) :recipient_email nil}))
         (concat (map (fn [x] {:recipient_id nil :recipient_email x}) (not-found-names found-users recipientUserIdsOrEmailAddresses)))
         (map (fn [sug] (assoc sug :swirl_id swirlId :code (uuid))))
         )))

(defn now [] (java.sql.Timestamp. (System/currentTimeMillis)))

(defn create-response [swirl-id summary author]
  (transaction
    (let [response
          (insert db/swirl-responses
                  (values {:swirl_id swirl-id :responder (:id author) :summary summary :date_responded (now)})
                  )]
      (update db/suggestions
              (set-fields {:response_id (response :id)})
              (where (or
                       {:swirl_id swirl-id :recipient_id (author :id)}
                       {:swirl_id swirl-id :recipient_email (author :email)}
                       )))
      response)))

(defn create-comment [swirld-id comment author]
  (insert db/comments
          (values {:swirl_id swirld-id :author_id (:id author) :html_content comment :date_responded (now)})
          ))

(defn save-draft-swirl [type author-id title review image-thumbnail optional-values]
  (insert db/swirls
          (values (merge {:type type :author_id author-id :title title :review review :thumbnail_url image-thumbnail :state "D"} optional-values))))

(defn add-suggestions [swirl-id author-id recipient-names-or-emails]
  (if (not-empty recipient-names-or-emails)
    (let [suggestions (create-suggestions recipient-names-or-emails swirl-id)
          recipient-ids (map #(% :recipient_id) (filter #(and (not (nil? (% :recipient_id))) (not= (% :recipient_id) author-id)) suggestions))]

      (doseq [sug suggestions]
        (try (insert db/suggestions (values sug))
             (catch PSQLException e (log/warn "Error while saving suggestion - okay to ignore if unique violation" e))))
      (networking/store-multiple author-id :knows recipient-ids)
      (doseq [recipient-id recipient-ids] (networking/store recipient-id :knows author-id)))))

(defn publish-swirl
  "Updates a draft Swirl to be live, and updates the user network and sends email suggestions. Returns true if id is a
  swirl belonging to the author; otherwise false."
  [swirl-id author-id title review recipient-names-or-emails]
  (let [updated (update db/swirls
                        (set-fields {:title title :review review :state "L"})
                        (where {:id swirl-id :author_id author-id}))]
    (add-suggestions swirl-id author-id recipient-names-or-emails)
    (= updated 1)))

(defn get-swirl [id]
  (first (select db/swirls
                 (fields :id :type :author_id :title :review :creation_date :itunes_collection_id :thumbnail_url :users.username :users.email_md5)
                 (join :inner db/users (= :users.id :swirls.author_id))
                 (where {:id id})
                 (limit 1))))

(defn get-swirl-responses [swirld-id]
  (select db/swirl-responses
          (fields :summary :users.username :users.email_md5 :responder)
          (join :inner db/users (= :users.id :swirl_responses.responder))
          (where {:swirl_id swirld-id})
          ))

(defn get-swirl-comments [swirld-id]
  (select db/comments
          (fields :html_content :users.username :users.email_md5 :date_responded)
          (join :inner db/users (= :users.id :comments.author_id))
          (where {:swirl_id swirld-id})
          (order :date_responded :asc)))

(defn get-recent-swirls [swirl-count skip]
  (select db/swirls
          (fields :type :creation_date, :review, :title, :id, :users.username :users.email_md5 :thumbnail_url)
          (join :inner db/users (= :swirls.author_id :users.id))
          (where {:state "L"})
          (offset skip)
          (limit swirl-count)
          (order :creation_date :desc)))

(defn get-swirls-authored-by [user-id]
  (select db/swirls
          (where {:author_id user-id :state "L"})
          (order :creation_date :desc)))


(defn get-swirls-awaiting-response [userId swirl-count skip]
  (select db/swirls
          (fields :type :creation_date, :review, :title, :id, :users.username :users.email_md5 :thumbnail_url)
          (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
          (join :inner db/users (= :swirls.author_id :users.id))
          (where {:suggestions.recipient_id userId :suggestions.response_id nil})
          (offset skip)
          (limit swirl-count)
          (order :creation_date :desc)))

(defn get-response-count-for-user [user-id]
  (db/query "SELECT summary, count(1) AS count FROM swirl_responses WHERE responder = ? GROUP BY summary ORDER BY summary" user-id))

(defn get-swirls-by-response [user-id swirl-count skip response]
  (db/query "SELECT swirls.type, swirls.creation_date, swirls.review, swirls.title, swirls.id, users.username, users.email_md5, swirls.thumbnail_url
  FROM (swirls INNER JOIN swirl_responses ON swirls.id = swirl_responses.swirl_id)
  INNER JOIN users ON swirls.author_id = users.id
  WHERE (swirl_responses.responder = ? AND LOWER(swirl_responses.summary) = ?)
  ORDER BY swirls.creation_date DESC
  LIMIT ? OFFSET ?" user-id (clojure.string/lower-case response) swirl-count skip))

(defn get-non-responders [swirl-id]
  (db/query "SELECT users.username, users.email_md5 FROM
  (suggestions INNER JOIN users ON users.id = suggestions.recipient_id)
  LEFT JOIN swirl_responses ON swirl_responses.swirl_id = suggestions.swirl_id AND swirl_responses.responder = suggestions.recipient_id
WHERE (suggestions.swirl_id = ? AND swirl_responses.id IS NULL)" swirl-id))

(defn get-suggestion-usernames [swirl-id]
  (select db/suggestions
          (fields :users.username [:users.id :user-id])
          (join :inner db/users (= :suggestions.recipient_id :users.id))
          (where {:swirl_id swirl-id})
          (order :users.username :asc)))
