(ns yswrl.user.notifications-repo
  (:require
    [yswrl.db :as db]))

(use 'korma.core)

(defn unseen-notifications-count [user-id]
  (:cnt (first (select db/notifications
          (aggregate (count :*) :cnt)
          (where {:target_user_id user-id})
          (where {:date_seen      nil})))))

(defn get-notifications-for-user [user-id]
  (-> (select* db/notifications)
      (fields :swirl_id :subject_id :target_user_id :notification_type [:swirls.title :swirl-title] :summary
              [:users.id :instigator-id] [:users.username :instigator-username] :date_seen :date_emailed :date_created)
      (join :inner db/swirls (= :notifications.swirl_id :swirls.id))
      (join :left db/users (= :notifications.instigator_id :users.id))
      (where {:target_user_id user-id})))

(defn get-for-user-email [user-id]
  (-> (get-notifications-for-user user-id)
      (where {:date_seen      nil
              :date_emailed   nil})
      (order :id :asc)
      (select)))

(defn get-for-user-page [user-id]
  (-> (get-notifications-for-user user-id)
      (limit 100)
      (order :id :desc)
      (select)))


