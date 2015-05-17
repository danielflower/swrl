(ns yswrl.swirls.swirls-repo
  (:require [yswrl.db :as db]
            [yswrl.user.networking :as networking]
            [yswrl.auth.auth-repo :as auth])
  )
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

(defn create-response [swirld-id summary author]
  (insert db/swirl-responses
          (values {:swirl_id swirld-id :responder (:id author) :summary summary :date_responded (now)})
          ))

(defn create-comment [swirld-id comment author]
  (insert db/comments
          (values {:swirl_id swirld-id :author_id (:id author) :html_content comment :date_responded (now)})
          ))

(defn save-draft-swirl [author-id title review image-thumbnail optional-values]
  (insert db/swirls
          (values (merge {:author_id author-id :title title :review review :thumbnail_url image-thumbnail :state "D"} optional-values))))

(defn publish-swirl
  "Updates a draft Swirl to be live, and updates the user network and sends email suggestions. Returns true if id is a
  swirl belonging to the author; otherwise false."
  [swirl-id author-id title review recipientNames]
  (transaction
    (let [updated (update db/swirls
                          (set-fields {:title title :review review :state "L"})
                          (where {:id swirl-id :author_id author-id}))]
      (if (not-empty recipientNames)
        (let [suggestions (create-suggestions recipientNames swirl-id)
              recipient-ids (map #(% :recipient_id) (filter #(and (not (nil? (% :recipient_id))) (not= (% :recipient_id) author-id)) suggestions))]
          (insert db/suggestions (values suggestions))
          (networking/store-multiple author-id :knows recipient-ids)
          (doseq [recipient-id recipient-ids] (networking/store recipient-id :knows author-id))))
      (= updated 1))))

(defn get-swirl [id]
  (first (select db/swirls
                 (where {:id id})
                 (limit 1))))

(defn get-swirl-responses [swirld-id]
  (select db/swirl-responses
          (fields :summary :users.username :responder)
          (join :inner db/users (= :users.id :swirl_responses.responder))
          (where {:swirl_id swirld-id})
          ))

(defn get-swirl-comments [swirld-id]
  (select db/comments
          (fields :html_content :users.username :date_responded)
          (join :inner db/users (= :users.id :comments.author_id))
          (where {:swirl_id swirld-id})
          (order :date_responded :asc)))

(defn get-recent-swirls [swirl-count skip]
  (select db/swirls
          (fields :creation_date, :review, :title, :id, :users.username :thumbnail_url)
          (join :inner db/users (= :swirls.author_id :users.id))
          (where {:state "L"})
          (offset skip)
          (limit swirl-count)
          (order :creation_date :asc)))

(defn get-swirls-authored-by [userId]
  (select db/swirls
          (where {:author_id userId :state "L"})))


(defn get-swirls-for [userId swirl-count skip]
  (select db/swirls
          (fields :creation_date, :review, :title, :id, :users.username :thumbnail_url)
          (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
          (join :inner db/users (= :swirls.author_id :users.id))
          (where {:suggestions.recipient_id userId})
          (offset skip)
          (limit swirl-count)
          (order :creation_date :asc)))
