(ns yswrl.swirls.lookups
  (:require [yswrl.db :as db]
            [yswrl.swirls.swirl-states :as states]
            [korma.core :refer [select* limit subselect aggregate offset order where join fields select raw modifier group]]
            [yswrl.user.networking :as network]))
(use 'korma.db)


; Queries to get a single swirl with author information
(defn select-single-swirl [id]
  (-> (select* db/swirls)
      (fields :id :external_id :type :author_id :title :review :creation_date :itunes_collection_id :thumbnail_url :users.username :users.email_md5 :is_private :swirl_details.details)
      (join :inner db/users (= :users.id :swirls.author_id))
      (join db/swirl-details (and
                               (= :swirl_details.external_id :swirls.external_id)
                               (= :swirl_details.type :swirls.type)))
      (where {:id id :state [not= states/deleted]})
      (limit 1)))

(defn get-swirl [id]
  (->> (-> (select-single-swirl id)
           (select))
       (map #(update % :details db/from-jsonb))
       first))


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
  (->> (-> (select-single-swirl id)
           (where-user-can-view requestor)
           (select))
       (map #(update % :details db/from-jsonb))
       first))

(defn get-swirl-if-allowed-to-edit [id user-id]
  (->> (-> (select-single-swirl id)
           (where {:author_id user-id})
           (select))
       (map #(update % :details db/from-jsonb))
       first))


; Queries to get multiple swirls

(defn multiple-live-swirls-admin
  "For use by admin functions only"
  []
  (-> (select* db/swirls)
      (join db/swirl-details (and
                               (= :swirl_details.external_id :swirls.external_id)
                               (= :swirl_details.type :swirls.type)))
      (where {:state states/live})))

(defn multiple-live-swirls [requestor]
  (-> (select* db/swirls)
      (join db/swirl-details (and
                               (= :swirl_details.external_id :swirls.external_id)
                               (= :swirl_details.type :swirls.type)))
      (where {:state states/live})                          ; this almost looks like duplication but stops a users draft and deleted swirls from showing in list views
      (where-user-can-view requestor)
      ))

(defn select-multiple-swirls [requestor max-results skip]
  (-> (multiple-live-swirls requestor)
      (fields :type :external_id :creation_date, :review, :title, :id, :users.username :users.email_md5 :thumbnail_url :author_id :is_private)
      (join :inner db/users (= :users.id :swirls.author_id))
      (offset skip)
      (limit max-results)))

(defn get-all-swirls [max-results skip requestor]
  (map
    #(update % :details db/from-jsonb)
    (-> (select-multiple-swirls requestor max-results skip)
        (fields :swirl_details.details)
        (order :id :desc)
        (select))))

(defn get-all-swirls-with-details [max-results skip requestor]
  (map
    #(update % :details (fn [s]
                          (assoc (db/from-jsonb s)
                            :website-url (:website-url %))))
    (-> (select-multiple-swirls requestor max-results skip)
        (fields :swirl_details.details [:swirl_links.code :website-url])
        (join db/swirl-links (and (= :swirls.id :swirl_links.swirl_id)
                                  (= "W" :swirl_links.type_code)))
        (where {:external_id           [not= nil]
                :swirl_details.details [not= nil]})
        (order :id :desc)
        (select))))

(defn get-home-swirls-with-weighting* [max-results skip requestor]
  (-> (select-multiple-swirls requestor max-results skip)
      (fields (raw (str "(10 + "
                        "CASE WHEN is_recipient AND has_responded THEN 10
                              WHEN is_recipient THEN (100 * is_recipient::int)
                              ELSE 0
                        END +"
                        "(30 * author_is_friend::int) + "
                        "(5 * number_of_comments) + "
                        "(20 * number_of_comments_from_friends) + "
                        "(15 * number_of_positive_responses) + "
                        "(30 * number_of_positive_responses_from_friends) - "
                        "(50 * is_author::int) - "
                        "CASE WHEN list_state = 'dismissed' THEN 10000
                              WHEN list_state = 'consuming' THEN 200
                              WHEN list_state = 'done' THEN 500
                              WHEN list_state = 'wishlist' THEN 150
                              ELSE 0
                        END - "
                        "(DATE_PART('epoch', now() - created) / 86400))"
                        " AS weighting"))
              :swirl_weightings.is_recipient :swirl_weightings.author_is_friend
              :swirl_weightings.number_of_comments :swirl_weightings.number_of_positive_responses
              :swirl_weightings.number_of_comments_from_friends :swirl_weightings.number_of_positive_responses_from_friends
              :swirl_weightings.is_author
              :swirl_details.details)
      (join :inner db/swirl-weightings (= :swirls.id :swirl_weightings.swirl_id))
      (where {:swirl_weightings.user_id (:id requestor)})
      (order (raw "weighting") :desc)))

(defn get-home-swirls-with-weighting [max-results skip requestor]
  (map
    #(update % :details db/from-jsonb)
    (-> (get-home-swirls-with-weighting* max-results skip requestor)
        (select))))

(defn get-weighted-swirls-with-external-id [max-results skip requestor]
  (-> (get-home-swirls-with-weighting* max-results skip requestor)
      (where {:external_id [not= nil]})
      (select)))

(defn search-for-swirls [max-results skip requestor search-query]
  (map
    #(update % :details db/from-jsonb)
    (if (or (nil? search-query) (clojure.string/blank? search-query))
      []
      (let [escaped-search-query (str "''" (clojure.string/escape search-query {\' ""}) "''" ":*")]
        (-> (select-multiple-swirls requestor max-results skip)
            (join :inner (raw "search_index") (= :swirls.id (raw "search_index.swirl_id")))
            (fields :swirl_details.details)
            (where (raw (str "search_index.document @@
        to_tsquery('english', '" escaped-search-query "')")))
            (order (raw (str "ts_rank(search_index.document, to_tsquery('english', '" escaped-search-query "'))")) :desc)
            (select))))))

(defn get-swirls-by-id [ids requestor]
  (map
    #(update % :details db/from-jsonb)
    (-> (select-multiple-swirls requestor 100 0)
        (fields :swirl_details.details)
        (where {:swirls.id [in ids]})
        (order :id :desc)
        (select))))

(defn get-swirls-authored-by [author-id requestor]
  (map
    #(update % :details db/from-jsonb)
    (-> (select-multiple-swirls requestor 1000 0)
        (fields :swirl_details.details)
        (where {:author_id author-id})
        (order :id :desc)
        (select))))

(defn get-swirls-authored-by-friends [requestor]
  (map
    #(update % :details db/from-jsonb)
    (let [friends (map :user-id (network/get-relations (requestor :id) :knows))]
      (-> (select-multiple-swirls requestor 100000 0)
          (fields :swirl_details.details)
          (where {:author_id [in friends]})
          (order :id :desc)
          (select)))))

(defn get-swirls-awaiting-response [requestor max-results skip]
  (map
    #(update % :details db/from-jsonb)
    (-> (select-multiple-swirls requestor max-results skip)
        (fields :swirl_details.details)
        (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
        (where {:suggestions.recipient_id (requestor :id) :suggestions.response_id nil})
        (order :id :desc)
        (select))))

(defn get-swirls-awaiting-response-count [requestor]
  (:cnt (first (-> (multiple-live-swirls requestor)
                   (aggregate (count :*) :cnt)
                   (join :inner db/suggestions (= :swirls.id :suggestions.swirl_id))
                   (where {:suggestions.recipient_id (requestor :id) :suggestions.response_id nil})
                   (select)))))


(defn get-swirls-by-response [requestor max-results skip response]
  (map
    #(update % :details db/from-jsonb)
    (-> (select-multiple-swirls requestor max-results skip)
        (fields :swirl_details.details)
        (join :inner db/swirl-responses (= :swirls.id :swirl_responses.swirl_id))
        (where {:swirl_responses.responder (requestor :id)})
        (where {(raw "LOWER(swirl_responses.summary)") (clojure.string/lower-case response)})
        (order :id :desc)
        (select))))

(defn get-swirls-in-user-swrl-list [requestor max-results skip user]
  (map
    #(update % :details db/from-jsonb)
    (-> (select-multiple-swirls requestor max-results skip)
        (fields :swirl_details.details)
        (join :inner db/swirl-lists (= :swirls.id :swirl_lists.swirl_id))
        (where {:swirl_lists.owner (user :id)})
        (fields [:swirl_lists.state :state] [:swirl_lists.date_added :date_added])
        (order :swirl_lists.date_added :desc)
        (select))))


(defn get-response-count-for-user [user-id]
  (db/query "SELECT r.summary, count(1) AS count FROM swirl_responses r INNER JOIN swirls s ON s.id = r.swirl_id WHERE s.state = ? AND r.responder = ? GROUP BY r.summary ORDER BY r.summary" states/live user-id))

