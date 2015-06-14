(ns yswrl.user.notifications-repo
  (:require
    [yswrl.db :as db]))

(use 'korma.core)

(defn get-for-user [user-id]
  (select db/notifications
          (fields :swirl_id :subject_id :target_user_id :notification_type [:swirls.title :swirl-title] :summary
                  [:users.id :instigator-id] [:users.username :instigator-username])
          (join :inner db/swirls (= :notifications.swirl_id :swirls.id))
          (join :left db/users (= :notifications.instigator_id :users.id))
          (where {:target_user_id user-id
                  :date_seen      nil
                  :date_emailed   nil
                  })
          (order :id :asc)))
