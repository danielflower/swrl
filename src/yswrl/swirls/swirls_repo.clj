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
            [korma.core :as k]
            [clj-time.core :as time]
            [yswrl.swirls.lookups :as lookups]
            [yswrl.swirls.types :as types]
            [yswrl.swirls.tmdb :as tmdb]
            [clojure.data.json :as json]
            [yswrl.swirls.amazon :as amazon]
            [yswrl.swirls.itunes :as itunes]
            [yswrl.swirls.website :as website])
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
      (if (-> (k/select db/positive-responses (k/fields :summary) (k/where {:summary summary}))
              count
              (> 0))
        (do (log/debug "START: update respond to swirl: " (time/now))
            (db/execute (str "UPDATE swirl_weightings sw
SET
number_of_positive_responses = (SELECT COUNT(1) FROM swirl_responses where swirl_id = sw.swirl_id and
                                            summary in (SELECT summary from positive_responses)),
number_of_positive_responses_from_friends = (SELECT COUNT(1) FROM swirl_responses r where r.swirl_id = sw.swirl_id and
                                             r.summary in (SELECT summary from positive_responses) and
                                             r.responder in (SELECT another_user_id from network_connections
                                                              where
                                                               user_id=sw.user_id and relation_type='knows'))
WHERE sw.swirl_id = " swirl-id))
            (log/debug "DONE: update respond to swirl: " (time/now))))
      (k/update db/suggestions
                (k/set-fields {:response_id (response :id)})
                (k/where (or
                           {:swirl_id swirl-id :recipient_id (author :id)}
                           {:swirl_id swirl-id :recipient_email (author :email)}
                           )))
      ; update the weightings table with the response
      (k/update db/swirl-weightings
                (k/set-fields {:has_responded true})
                (k/where {:swirl_id swirl-id
                          :user_id  (:id author)}))
      response)))

(defn add-swirl-to-wishlist [swirl-id state owner]
  (while (= 0 (k/update db/swirl-lists
                        (k/set-fields {:swirl_id swirl-id :owner (:id owner) :state state :date_added (now)})
                        (k/where {:swirl_id swirl-id :owner (:id owner)})))
    (k/insert db/swirl-lists
              (k/values {:swirl_id swirl-id :owner (:id owner) :state state :date_added (now)}))
    (k/update db/swirl-weightings
              (k/set-fields {:list_state state})
              (k/where {:swirl_id swirl-id :user_id (:id owner)}))))


(defn remove-from-watchlist [swirl-id user]
  (k/delete db/swirl-lists
            (k/where {:swirl_id swirl-id :owner (:id user)}))
  (k/update db/swirl-weightings
            (k/set-fields {:list_state nil})
            (k/where {:swirl_id swirl-id :user_id (:id user)})))

(defn create-comment [swirl-id comment author]
  (let [comment (k/insert db/comments
                          (k/values {:swirl_id swirl-id :author_id (:id author) :html_content comment :date_responded (now)})
                          )]
    (k/update db/swirl-weightings
              (k/set-fields {:number_of_comments (k/raw "1 + number_of_comments")})
              (k/where {:swirl_id swirl-id}))
    (log/debug "START: create-comment: " (time/now))
    (db/execute (str "UPDATE swirl_weightings sw
SET
number_of_comments_from_friends = (SELECT COUNT(1) FROM comments c
                                   where c.swirl_id = sw.swirl_id and
                                   c.author_id in
                                   (SELECT another_user_id from network_connections
                                       where
                                       user_id=sw.user_id and relation_type='knows'))
WHERE sw.swirl_id = " swirl-id))
    (log/debug "END: create-comment: " (time/now))
    comment))

(defn save-details [swirl-details external_id type]
  (if (and (not= nil swirl-details)
           (not= nil external_id))
    (if (= 0 (k/update db/swirl-details
                       (k/set-fields {:external_id external_id :type type :details (db/as-jsonb swirl-details)})
                       (k/where {:external_id external_id :type type})))
      (k/insert db/swirl-details
                (k/values {:external_id external_id :type type :details (db/as-jsonb swirl-details)})))))

(defn save-draft-swirl
  "Returns the swirl if created"
  [swirl-details type author-id title review image-thumbnail external_id]
  (let [swirl (k/insert db/swirls
                        (k/values {:type       type :author_id author-id :title title :external_id external_id
                                   :review     review :thumbnail_url image-thumbnail :state states/draft
                                   :is_private false}))
        external_id (if-let [external_id external_id]
                      (str external_id))]
    (try (save-details swirl-details external_id type)
         (catch Exception e
           (log/error e "Couldn't save details for swirl: " swirl " with details: " swirl-details)))
    ;now add the new rows into the weighting table
    (k/insert db/swirl-weightings
              (k/values (k/select db/users
                                  (k/fields [(k/raw (:id swirl)) :swirl_id]
                                            [:users.id :user_id]
                                            [(k/raw (str "(id = " author-id ")")) :is_author]))))
    swirl))

(defn add-link [swirl-id link-type-code link-value]
  (k/insert db/swirl-links
            (k/values {:swirl_id swirl-id :type_code link-type-code :code link-value})))

(defn get-links [swirl-id]
  (let [links (k/select db/swirl-links (k/where {:swirl_id swirl-id}))]
    (map #(assoc % :type (swirl-links/link-type-of (% :type_code))) links)))

(defn update-weightings-for-friend-changes [users]
  (db/execute (format "
with all_network_conns as (select * from network_connections),
all_comments as (select * from comments),
all_responses as (select * from swirl_responses),
all_positive_responses as (select * from positive_responses)
UPDATE swirl_weightings sw
SET
author_is_friend = EXISTS(SELECT 1 from all_network_conns
                              WHERE another_user_id = s.author_id
                             AND user_id = sw.user_id
                             AND relation_type = 'knows'),
number_of_comments_from_friends = (SELECT COUNT(1) FROM all_comments c
                                   where c.swirl_id = s.id and
                                   c.author_id in
                                   (SELECT another_user_id from all_network_conns
                                       where
                                       user_id=sw.user_id and relation_type='knows')),
number_of_positive_responses_from_friends = (SELECT COUNT(1) FROM all_responses r where r.swirl_id = s.id and
                                             r.summary in (SELECT summary from all_positive_responses) and
                                             r.responder in (SELECT another_user_id from all_network_conns
                                                              where
                                                               user_id=sw.user_id and relation_type='knows'))
FROM swirls s
WHERE s.id = sw.swirl_id
AND sw.user_id IN (SELECT another_user_id from all_network_conns
                   WHERE user_id IN (%s));" (clojure.string/join "," users))))



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
        (setup-network-links-and-notifications swirl-id author-id recipient-id))
      ;update the weightings table to reflect these changes
      (k/update db/swirl-weightings
                (k/set-fields {:is_recipient true})
                (k/where {:swirl_id swirl-id
                          :user_id  [in recipient-ids]}))
      )))

(defn publish-swirl
  "Updates a draft Swirl to be live, and updates the user network and sends email suggestions. Returns true if id is a
  swirl belonging to the author; otherwise false."
  [swirl-id author-id title review recipient-names-or-emails private? type image-url]
  (let [updated (k/update db/swirls
                          (k/set-fields {:title title :review review :state states/live :is_private private? :type type :thumbnail_url image-url})
                          (k/where {:id swirl-id :author_id author-id}))]
    ; update the updated timestamp on the weightings table
    (k/update db/swirl-weightings
              (k/set-fields {:updated (now)})
              (k/where {:swirl_id swirl-id}))
    (add-suggestions swirl-id author-id recipient-names-or-emails)
    (db/execute "REFRESH MATERIALIZED VIEW search_index")   ; WARNING: the whole index is refreshed on every update
    ; now update the weightings table
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

(defn update-movie-external-ids []
  (log/info "Updating movie external IDs")
  (let [swrls-with-no-external-id (-> (lookups/multiple-live-swirls-admin)
                                      (k/where {:external_id nil
                                                :type        "movie"})
                                      (k/select))]
    (doseq [swrl swrls-with-no-external-id]
      (log/info "updating external id for: " swrl)
      (if-let [movie-link (->> (get-links (:id swrl))
                               (filter #(= "M" (:type_code %)))
                               first)]
        (do
          (log/info "movie link: " movie-link)
          (let [tmdb-id (:tmdb-id (tmdb/get-tmdb-id-from-imdb-id (:code movie-link)))]
            (log/info "tmdb id: " tmdb-id)
            (k/update db/swirls
                      (k/set-fields {:external_id tmdb-id})
                      (k/where {:id (:id swrl)})))))))
  (log/info "Finished updating movie external IDs"))

(defn update-album-external-ids []
  (log/info "Updating album external IDs")
  (let [swrls-with-no-external-id (-> (lookups/multiple-live-swirls-admin)
                                      (k/where {:external_id nil
                                                :type        "album"})
                                      (k/select))]
    (doseq [swrl swrls-with-no-external-id]
      (log/info "updating external id for: " swrl)
      (if-let [itunes-link (->> (get-links (:id swrl))
                                (filter #(= "I" (:type_code %)))
                                first)]
        (do
          (log/info "itunes link: " itunes-link)
          (k/update db/swirls
                    (k/set-fields {:external_id (:code itunes-link)})
                    (k/where {:id (:id swrl)}))))))
  (log/info "Finished updating album external IDs"))

(defn update-book-external-ids []
  (log/info "Updating book external IDs")
  (let [swrls-with-no-external-id (-> (lookups/multiple-live-swirls-admin)
                                      (k/where {:external_id nil
                                                :type        "book"})
                                      (k/select))]
    (doseq [swrl swrls-with-no-external-id]
      (log/info "updating external id for: " swrl)
      (if-let [asin-link (->> (get-links (:id swrl))
                              (filter #(= "A" (:type_code %)))
                              first)]
        (do
          (log/info "asin link: " asin-link)
          (k/update db/swirls
                    (k/set-fields {:external_id (:code asin-link)})
                    (k/where {:id (:id swrl)}))))))
  (log/info "Finished updating book external IDs"))

(defn update-game-external-ids []
  (log/info "Updating game external IDs")
  (let [swrls-with-no-external-id (-> (lookups/multiple-live-swirls-admin)
                                      (k/where {:external_id nil
                                                :type        "game"})
                                      (k/select))]
    (doseq [swrl swrls-with-no-external-id]
      (log/info "updating external id for: " swrl)
      (if-let [asin-link (->> (get-links (:id swrl))
                              (filter #(= "A" (:type_code %)))
                              first)]
        (do
          (log/info "asin link: " asin-link)
          (k/update db/swirls
                    (k/set-fields {:external_id (:code asin-link)})
                    (k/where {:id (:id swrl)}))))))
  (log/info "Finished updating game external IDs"))

(defn update-website-external-ids []
  (log/info "Updating website external IDs")
  (let [swrls-with-no-external-id (-> (lookups/multiple-live-swirls-admin)
                                      (k/where {:external_id nil
                                                :type        "website"})
                                      (k/select))]
    (doseq [swrl swrls-with-no-external-id]
      (log/info "updating external id for: " swrl)
      (if-let [web-link (->> (get-links (:id swrl))
                             (filter #(= "W" (:type_code %)))
                             first)]
        (do
          (log/info "web link: " web-link)
          (k/update db/swirls
                    (k/set-fields {:external_id (:code web-link)})
                    (k/where {:id (:id swrl)}))))))
  (log/info "Finished updating website external IDs"))

(defn update-video-external-ids []
  (log/info "Updating video external IDs")
  (let [swrls-with-no-external-id (-> (lookups/multiple-live-swirls-admin)
                                      (k/where {:external_id nil
                                                :type        "video"})
                                      (k/select))]
    (doseq [swrl swrls-with-no-external-id]
      (log/info "updating external id for: " swrl)
      (if-let [web-link (->> (get-links (:id swrl))
                             (filter #(= "W" (:type_code %)))
                             first)]
        (do
          (log/info "web link: " web-link)
          (k/update db/swirls
                    (k/set-fields {:external_id (:code web-link)})
                    (k/where {:id (:id swrl)}))))))
  (log/info "Finished updating video external IDs"))

(defn update-all-details []
  (log/info "Updating all details")
  (let [details-to-update (-> (lookups/multiple-live-swirls-admin)
                              (k/where {:external_id [not= nil]})
                              (k/fields :external_id :type)
                              (k/select)
                              set)]
    (doseq [detail-to-update details-to-update]
      (if-let [details (try (case (:type detail-to-update)
                              "book" (amazon/get-book (:external_id detail-to-update))
                              "game" (amazon/get-game (:external_id detail-to-update))
                              "album" (itunes/get-itunes-album (:external_id detail-to-update))
                              "video" (website/get-metadata (:external_id detail-to-update))
                              "movie" (tmdb/get-movie-from-tmdb-id (:external_id detail-to-update))
                              "tv" (tmdb/get-tv-from-tmdb-id (:external_id detail-to-update))
                              "website" (website/get-metadata (:external_id detail-to-update))
                              "podcast" (itunes/get-itunes-podcast (:external_id detail-to-update))
                              "app" (itunes/get-itunes-app (:external_id detail-to-update))
                              nil)
                            (catch Exception _ nil))]
        (do
          (log/info "updating " detail-to-update)
          (save-details details (:external_id detail-to-update) (:type detail-to-update)))))
    (log/info "Finished updating all details")))