(ns yswrl.swirls.swirls-repo
  (:require [yswrl.db :as db]
            [yswrl.user.networking :as networking]
            [yswrl.auth.auth-repo :as auth]
            [clojure.tools.logging :as log]
            [yswrl.swirls.swirl-links :as swirl-links]
            [yswrl.swirls.lookups :refer :all]
            [yswrl.swirls.swirl-states :as states]
            [yswrl.user.notifications :as notifications]
            [yswrl.utils :as utils]
            [korma.core :as k])
  (:import (org.postgresql.util PSQLException)))
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
  (first (k/select db/suggestions
                 (k/fields :recipient_id :recipient_email)
                 (k/where {:code code}))))

(defn get-suggestion-usernames [swirl-id]
  (k/select db/suggestions
          (k/fields :users.username [:users.id :user-id] :users.email_md5)
          (k/join :inner db/users (= :suggestions.recipient_id :users.id))
          (k/where {:swirl_id swirl-id})
          (k/order :users.username :asc)))

(defn create-suggestions [recipientUserIdsOrEmailAddresses swirlId]
  (let [already-suggested (map :username (get-suggestion-usernames swirlId))
        found-users (auth/get-users-by-username_or_email (distinct recipientUserIdsOrEmailAddresses))
        not-found-users (not-found-names found-users recipientUserIdsOrEmailAddresses)]
    (->> (filter #(not (utils/in? already-suggested (% :username))) found-users)
         (map (fn [user] {:recipient_id (:id user) :recipient_email nil}))
         (concat (map (fn [x] {:recipient_id nil :recipient_email x}) not-found-users))
         (map (fn [sug] (assoc sug :swirl_id swirlId :code (uuid))))
         )))

(defn now [] (java.sql.Timestamp. (System/currentTimeMillis)))

(defn respond-to-swirl [swirl-id summary author]
  (transaction
    (let [response
          (if (= 0 (k/update db/swirl-responses (k/set-fields {:summary summary}) (k/where {:swirl_id swirl-id :responder (author :id)})))
            (k/insert db/swirl-responses
                    (k/values {:swirl_id swirl-id :responder (:id author) :summary summary :date_responded (now)}))
            (first (k/select db/swirl-responses (k/where {:swirl_id swirl-id :responder (author :id)}))))]
      (k/update db/suggestions
              (k/set-fields {:response_id (response :id)})
              (k/where (or
                       {:swirl_id swirl-id :recipient_id (author :id)}
                       {:swirl_id swirl-id :recipient_email (author :email)}
                       )))
      response)))

(defn add-swirl-to-wishlist [swirl-id state owner]
  (if (= 0 (k/update db/swirl-lists
                 (k/set-fields {:swirl_id swirl-id :owner (:id owner) :state state :date_added (now)})
                 (k/where {:swirl_id swirl-id :owner (:id owner)})))
    (k/insert db/swirl-lists
              (k/values {:swirl_id swirl-id :owner (:id owner) :state state :date_added (now)}))))


(defn remove-from-watchlist [swirl-id user]
  (k/delete db/swirl-lists
            (k/where {:swirl_id swirl-id :owner (:id user)})))

(defn create-comment [swirld-id comment author]
  (k/insert db/comments
          (k/values {:swirl_id swirld-id :author_id (:id author) :html_content comment :date_responded (now)})
          ))

(defn save-draft-swirl [type author-id title review image-thumbnail]
  (k/insert db/swirls
          (k/values {:type type :author_id author-id :title title
                   :review review :thumbnail_url image-thumbnail :state states/draft
                   :is_private false})))

(defn add-link [swirl-id link-type-code link-value]
  (k/insert db/swirl-links
          (k/values {:swirl_id swirl-id :type_code link-type-code :code link-value})))

(defn get-links [swirl-id]
  (let [links (k/select db/swirl-links (k/where {:swirl_id swirl-id}))]
    (map #(assoc % :type (swirl-links/link-type-of (% :type_code))) links)))

(defn setup-network-links-and-notifications [swirl-id author-id other-user-id]
  (networking/store other-user-id :knows author-id)
  (notifications/add notifications/recommendation other-user-id swirl-id swirl-id author-id))

(defn add-suggestions [swirl-id author-id recipient-names-or-emails]
  (if (not-empty recipient-names-or-emails)
    (let [suggestions (create-suggestions recipient-names-or-emails swirl-id)
          recipient-ids (map #(% :recipient_id) (filter #(and (not (nil? (% :recipient_id))) (not= (% :recipient_id) author-id)) suggestions))]

      (doseq [sug suggestions]
        (try (k/insert db/suggestions (k/values sug))
             (catch PSQLException e (if (.contains (.getMessage e) "duplicate key value violates unique constraint")
                                      (log/info "Duplicate suggestion detected. Form was probably submitted twice. Okay to ignore.")
                                      (log/warn "Error while saving suggestion" e)))))
      (networking/store-multiple author-id :knows recipient-ids)
      (doseq [recipient-id recipient-ids]
        (setup-network-links-and-notifications swirl-id author-id recipient-id)))))

(defn publish-swirl
  "Updates a draft Swirl to be live, and updates the user network and sends email suggestions. Returns true if id is a
  swirl belonging to the author; otherwise false."
  [swirl-id author-id title review recipient-names-or-emails private? type image-url]
  (let [updated (k/update db/swirls
                        (k/set-fields {:title title :review review :state states/live :is_private private? :type type :thumbnail_url image-url})
                        (k/where {:id swirl-id :author_id author-id}))]
    (add-suggestions swirl-id author-id recipient-names-or-emails)
    (db/execute "REFRESH MATERIALIZED VIEW search_index")   ; WARNING: the whole index is refreshed on every update
    (= updated 1)))

(defn delete-swirl
  "Deletes the swirl with the given ID if the deleter-id is the author. Returns the swirl-id if it was deleted, otherwise nil"
  [swirl-id deleter-id]
  (if (= 1 (k/update db/swirls
                   (k/set-fields {:state states/deleted})
                   (k/where {:id swirl-id :author_id deleter-id})))
    swirl-id
    nil))



; response and comment stuff

(defn get-swirl-responses [swirld-id responses-to-exclude]
  (k/select db/swirl-responses
          (k/fields :summary :users.username :users.email_md5 :responder :date_responded)
          (k/join :inner db/users (= :users.id :swirl_responses.responder))
          (k/where {:swirl_id swirld-id (k/raw "LOWER(summary)") [not-in responses-to-exclude]})))

(defn get-swirl-comments
  ([swirl-id]
   (get-swirl-comments swirl-id 0))
  ([swirld-id id-to-start-after]
   (k/select db/comments
           (k/fields :id :html_content :users.username :users.email_md5 :date_responded)
           (k/join :inner db/users (= :users.id :comments.author_id))
           (k/where {:swirl_id swirld-id :id [> id-to-start-after]})
           (k/order :date_responded :asc))))


(defn get-recent-responses-by-user-and-type [user-id swirl-type excluded]
  (map #(% :summary) (k/select db/swirl-responses
                             (k/fields :summary)
                             (k/join :inner db/swirls (= :swirls.id :swirl_responses.swirl_id))
                             (k/where {:responder user-id :swirls.type swirl-type :summary [not-in excluded]})
                             (k/order :date_responded :desc)
                             (k/limit 5))))

(defn get-non-responders [swirl-id]
  (db/query "SELECT users.username, users.email_md5 FROM
  (suggestions INNER JOIN users ON users.id = suggestions.recipient_id)
  LEFT JOIN swirl_responses ON swirl_responses.swirl_id = suggestions.swirl_id AND swirl_responses.responder = suggestions.recipient_id
WHERE (suggestions.swirl_id = ? AND swirl_responses.id IS NULL)" swirl-id))

