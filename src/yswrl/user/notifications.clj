(ns yswrl.user.notifications
  (:require
    [yswrl.layout :as layout]
    [compojure.core :refer [defroutes GET POST]]
    [ring.util.response :refer [status redirect response not-found]]
    [yswrl.auth.guard :as guard]
    [yswrl.db :as db]
    [yswrl.swirls.postman :as postman]
    [yswrl.swirls.lookups :as lookups]
    [korma.core
     :as k
     :refer [insert values where join fields set-fields select raw modifier]]
    [yswrl.user.notifications-repo :as notifications-repo])
  (:import (java.sql Timestamp)))


(def ^:const recommendation "R")
(def ^:const new-response "P")
(def ^:const new-comment "C")
(def ^:const added-to-group "G")

(defn user-from-session [req] (:user (:session req)))
(defn now [] (Timestamp. (System/currentTimeMillis)))

(defn add [notification-type target-user-id swirl-id subject-id instigator-id]
  (insert db/notifications (values {
                                    :target_user_id    target-user-id
                                    :notification_type notification-type
                                    :swirl_id          swirl-id
                                    :subject_id        subject-id
                                    :instigator_id     instigator-id
                                    })))

(defn user-ids-watching-swirl [swirl-id user-id-to-exclude]
  (db/query "SELECT DISTINCT id FROM users WHERE id IN (
  SELECT author_id FROM swirls WHERE swirls.id = ?
  UNION
  SELECT author_id FROM comments WHERE swirl_id = ?
  UNION
  SELECT recipient_id FROM suggestions WHERE swirl_id = ?
)
AND id != ?" swirl-id swirl-id swirl-id user-id-to-exclude))

(defn add-to-watchers-of-swirl [notification-type swirl-id subject-id instigator-id summary]
  (let [user-ids-to-notify (user-ids-watching-swirl swirl-id instigator-id)]
    (doall (map
             #(insert db/notifications
                      (values {:target_user_id    (% :id)
                               :notification_type notification-type
                               :swirl_id          swirl-id
                               :subject_id        subject-id
                               :instigator_id     instigator-id
                               :summary           summary
                               }))
             user-ids-to-notify)
           )))

(defn users-with-pending-notifications
  []
  (select db/users
          (modifier "DISTINCT")
          (fields :id :username :email :email_md5)
          (join :inner db/notifications (= :users.id :notifications.target_user_id))
          (where {:notifications.date_seen    nil
                  :notifications.date_emailed nil})
          (where (or {:date_last_emailed nil}
                     {(raw "COALESCE(date_last_emailed, date_registered)") [< (raw "(now() - interval '1 day')")]}))))


(defn mark-as-seen [swirl-id seer]
  (if seer
    (k/update db/notifications
            (set-fields {:date_seen (now)})
            (where {:swirl_id       swirl-id
                    :target_user_id (seer :id)
                    :date_seen      nil}
                   ))
    0))

(defn mark-subject-as-seen [swirl-id seer]
  (if seer
    (k/update db/notifications
            (set-fields {:date_seen (now)})
            (where {:subject_id     swirl-id
                    :target_user_id (seer :id)
                    :date_seen      nil}
                   ))
    0))

(defn clear-notifications-for-user-by-notification-type [user-id notification-type]
  (k/update db/notifications
          (set-fields {:date_seen (now)})
          (where {:target_user_id    user-id
                  :notification_type notification-type
                  :date_seen         nil})))

(defn mark-email-sent
  ([seer]
   (mark-email-sent seer (now)))
  ([seer timestamp]
   (k/update db/notifications
           (set-fields {:date_emailed timestamp})
           (where {:target_user_id (seer :id)
                   :date_emailed   nil}))
   (k/update db/users
           (set-fields {:date_last_emailed timestamp})
           (where {:id (seer :id)}))))

(defn group-by-swirl [notifications]
  (let [swirl-id-to-notifications (group-by #(% :swirl_id) notifications)]
    (vec (filter #(not (nil? (% :swirl)))
                 (map (fn [[swirl-id notifications]]
                        {:swirl         (lookups/get-swirl-if-allowed-to-view swirl-id {:id (:target_user_id (first notifications))})
                         :notifications notifications
                         })
                      swirl-id-to-notifications)))))


(defn create-notification-email-body [recipient notes]
  (postman/email-body "notifications/notification-email.html"
                      {:recipient recipient
                       :notifications (group-by-swirl notes)
                       :non-swirl-notifications (filter #(nil? (% :swirl_id)) notes)}))

(defn send-pending-notifications
  "email pending notifications"
  []
  (let [users (users-with-pending-notifications)]
    (doseq [user users]
      (let [notifications (notifications-repo/get-for-user-email (user :id))
            html (create-notification-email-body user notifications)]
        (mark-email-sent user)
        (postman/send-email (:email user) (:username user) "Swirl updates" html)))))

(defn get-notification-view-model [user]
  (let [raw (notifications-repo/get-for-user-page (user :id))]
    (vec (filter #(not (and (not (nil? (get-in % [:note :swirl_id]))) (nil? (% :swirl)))) ; if a swirl ID was on the notification, but the looked-up swirl is nil, then the user does not have permission to view it or it was deleted
                 (map (fn [n] {:note  n
                               :swirl (if (nil? (n :swirl_id)) nil (lookups/get-swirl-if-allowed-to-view (n :swirl_id) user))}) raw)))))

(defn get-notifications-and-mark-responses-as-seen-for [user]
  (let [notes (get-notification-view-model user)]
    (clear-notifications-for-user-by-notification-type (user :id) new-response)
    notes))

(defn view-notifications-page [user]
  (let [notes (get-notifications-and-mark-responses-as-seen-for user)]
    (layout/render "notifications/view-all.html" {:title         "What's new"
                                                  :pageTitle     "What's new"
                                                  :notifications notes})))

(defroutes notification-routes
           (GET "/notifications" [:as req] (guard/requires-login #(view-notifications-page (user-from-session req)))))
