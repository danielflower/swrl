(ns yswrl.user.notifications-repo
  (:require
    [yswrl.db :as db]))

(use 'korma.core)

(defn get-notifications-for-user [user-id]
  (-> (select* db/notifications)
      (fields :swirl_id :subject_id :target_user_id :notification_type [:swirls.title :swirl-title] :summary
              [:users.id :instigator-id] [:users.username :instigator-username] :date_seen :date_emailed)
      (join :inner db/swirls (= :notifications.swirl_id :swirls.id))
      (join :left db/users (= :notifications.instigator_id :users.id))
      (where {:target_user_id user-id})
      (order :id :asc)))

(defn get-for-user-email [user-id]
  (-> (get-notifications-for-user user-id)
      (where {:date_seen      nil
              :date_emailed   nil})
      (select)))

(defn get-for-user-page [user-id]
  (-> (get-notifications-for-user user-id)
      (limit 100)
      (select)))
