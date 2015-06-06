(ns yswrl.user.notifications
  (:require
    [yswrl.layout :as layout]
    [compojure.core :refer [defroutes GET POST]]
    [yswrl.links :as links]
    [ring.util.response :refer [status redirect response not-found]]
    [clojure.tools.logging :as log]
    [yswrl.auth.guard :as guard]
    [yswrl.db :as db]
    [yswrl.swirls.postman :as postman]))
(use 'korma.core)


(def ^:const recommendation "R")

(defn user-from-session [req] (:user (:session req)))
(defn now [] (java.sql.Timestamp. (System/currentTimeMillis)))

(defn add [notification-type target-user-id swirl-id subject-id]
  (insert db/notifications (values {
                                    :target_user_id    target-user-id
                                    :notification_type notification-type
                                    :swirl_id          swirl-id
                                    :subject_id        subject-id
                                    })))

(defn get-for-user [user-id]
  (select db/notifications
          (fields :swirl_id :subject_id :target_user_id :notification_type [:swirls.title :swirl-title])
          (join :inner db/swirls (= :notifications.swirl_id :swirls.id))
          (where {
                  :target_user_id user-id
                  :date_seen      nil
                  })))

(defn users-with-pending-notifications
  []
  (select db/users
          (modifier "DISTINCT")
          (fields :id :username :email_md5)
          (join :inner db/notifications (= :users.id :notifications.target_user_id))
          (where {:notifications.date_seen    nil
                  :notifications.date_emailed nil})
          (where (or {:date_last_emailed nil}
                     {:date_last_emailed [< (raw "(now() - interval '1 day')")]}))))

(defn view-notifications-page [user]
  (layout/render "notifications/view-all.html" {
                                                :title         "What's new"
                                                :pageTitle     "What's new"
                                                :notifications (get-for-user (user :id))})
  )

(defn mark-as-seen [swirl-id seer]
  (if seer
    (update db/notifications
            (set-fields {:date_seen (now)})
            (where {:swirl_id       swirl-id
                    :target_user_id (seer :id)
                    :date_seen      nil}
                   ))
    0))

(defn mark-email-sent
  ([seer]
   (mark-email-sent seer (now)))
  ([seer timestamp]
   (update db/notifications
           (set-fields {:date_emailed timestamp})
           (where {:target_user_id (seer :id)
                   :date_emailed   nil}))
   (update db/users
           (set-fields {:date_last_emailed timestamp})
           (where {:id (seer :id)}))
    ))



(defroutes notification-routes
           (GET "/notifications" [:as req] (guard/requires-login #(view-notifications-page (user-from-session req)))))

(defn create-notification-email-body [recipient notes]
  (postman/email-body "notifications/notification-email.html" {:recipient recipient :notes notes}))

(defn send-pending-notifications
  "email pending notifications"
  []
  (let [users (users-with-pending-notifications)]
    (doseq [user users]
      (let [notifications (get-for-user (user :id))
            html (create-notification-email-body user notifications)
            to [{:email (:email user) :name (:username user)}]]
        (mark-email-sent user)
        (postman/send-email to "Swirl updates" html)))))
