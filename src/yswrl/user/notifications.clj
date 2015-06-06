(ns yswrl.user.notifications
  (:require
    [yswrl.layout :as layout]
    [compojure.core :refer [defroutes GET POST]]
    [yswrl.links :as links]
    [ring.util.response :refer [status redirect response not-found]]
    [clojure.tools.logging :as log]
    [yswrl.auth.guard :as guard]
    [yswrl.db :as db]))
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
                    :date_seen nil}

                   ))
    0))

(defroutes notification-routes
           (GET "/notifications" [:as req] (guard/requires-login #(view-notifications-page (user-from-session req)))))

