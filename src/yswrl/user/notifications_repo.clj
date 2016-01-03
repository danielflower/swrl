(ns yswrl.user.notifications-repo
  (:require
    [yswrl.db :as db]
    [korma.core :as k]))


(defn unseen-notifications-count [user-id]
  (:cnt (first (k/select db/notifications
          (k/aggregate (count :*) :cnt)
          (k/where {:target_user_id user-id})
          (k/where {:date_seen      nil})))))

(defn get-notifications-for-user [user-id]
  (-> (k/select* db/notifications)
      (k/fields :swirl_id :subject_id :target_user_id :notification_type [:swirls.title :swirl-title] :summary
              [:users.id :instigator-id] [:users.username :instigator-username] [:users.email_md5 :instigator-email-md5] :date_seen :date_emailed :date_created)
      (k/join :left db/swirls (= :notifications.swirl_id :swirls.id))
      (k/join :left db/users (= :notifications.instigator_id :users.id))
      (k/where {:target_user_id user-id})))

(defn get-for-user-email [user-id]
  (-> (get-notifications-for-user user-id)
      (k/where {:date_seen      nil
              :date_emailed   nil})
      (k/order :id :asc)
      (k/select)))

(defn get-for-user-page [user-id]
  (-> (get-notifications-for-user user-id)
      (k/limit 100)
      (k/order (k/raw "(date_seen is null)") :desc)         ; show new notes first....
      (k/order :id :desc)                                   ; ...then order by date created
      (k/select)))


