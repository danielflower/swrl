(ns yswrl.user.preference-routes
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [yswrl.db :as db]
            [yswrl.swirls.postman :as postman]
            [ring.util.response :refer [redirect response]]
            [yswrl.auth.guard :as guard]
            [yswrl.auth.auth-repo :as users]
            [korma.core
             :as k
             :refer [insert values where join fields set-fields select raw modifier]])
  (:import (org.postgresql.util PGInterval)))

(defn notification-options-page [email]
  ; TODO: should really figure out current value (if logged in) and pre-select the correct radio button. Currently it just hard codes values in the HTML ignoring current setting
  (layout/render "users/notification-options.html" {:blacklist-email email}))

(defn blacklist-email [email]
  (if (clojure.string/blank? email)
    (notification-options-page "")
    (do
      (postman/blacklist email)
      (layout/render "users/updated.html" {:message (str "Your email address " email " has been added to our blacklist and you will not be emailed again.")}))))

(defn session-from [req] (:user (:session req)))

(defn key-to-interval [key]
  ; by having a fixed map of options, malicious users cannot insert malicious values
  (PGInterval. (case key
                 "1day" "1 day"
                 "1week" "7 days"
                 "1month" "1 month"
                 "1year" "1 year"
                 "1 week"
                 )))

(defn update-notification-preferences [user notification-interval inbox-interval]
  (k/update db/users
          (set-fields {:notification_email_interval (key-to-interval notification-interval)
                       :inbox_email_interval        (key-to-interval inbox-interval)})
          (where {:id (user :id)}))
  (layout/render "users/updated.html" {:message "Your notification preferences have been updated."}))



(defn edit-avatar-page [user]
  (layout/render "users/avatar-options.html" {:user user}))

(defn update-avatar-preferences [user avatar-type]
  (users/update-avatar-type (:id user) avatar-type)
  (layout/render "users/updated.html" {:message "Your avatar preferences have been updated."}))

(defroutes preference-routes
           (GET "/notification-options" [email] (notification-options-page email))
           (GET "/edit-avatar" [:as req] (guard/requires-login #(edit-avatar-page (session-from req))))
           (POST "/update-avatar-preferences" [avatar-type  :as req] (guard/requires-login #(update-avatar-preferences (session-from req) avatar-type)))
           (POST "/blacklist-email" [email-for-blacklist] (blacklist-email email-for-blacklist))
           (POST "/update-notification-preferences" [notification-interval inbox-interval :as req] (guard/requires-login #(update-notification-preferences (session-from req) notification-interval inbox-interval))))
