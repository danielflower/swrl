(ns yswrl.swirls.swirls-repo
  (:require [yswrl.db :as db]
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

(defn create-suggestions [recipientUserIdsOrEmailAddresses swirlId]
  (let [found-users (auth/get-users-by-username_or_email (distinct recipientUserIdsOrEmailAddresses))]
    (->> found-users
         (map (fn [user] {:recipient_id (:id user) :recipient_email nil}))
         (concat (map (fn [x] {:recipient_id nil :recipient_email x}) (not-found-names found-users recipientUserIdsOrEmailAddresses)))
         (map (fn [sug] (assoc sug :swirl_id swirlId :code (uuid))))
         )))

(defn create-swirl [authorId title review recipientNames]
  (transaction
    (let [swirl (insert db/swirls
                        (values {:author_id authorId :title title :review review}))
          ]
      (insert db/suggestions (values (create-suggestions recipientNames (:id swirl))))
      swirl)))

(defn get-swirl [id]
  (first (select db/swirls
                 (where {:id id})
                 (limit 1))))

(defn get-swirls-authored-by [userId]
  (select db/swirls
          (where {:author_id userId})))
