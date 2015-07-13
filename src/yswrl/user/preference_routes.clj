(ns yswrl.user.preference-routes
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [yswrl.db :as db]
            [yswrl.swirls.postman :as postman]
            [ring.util.response :refer [redirect response]]
            [yswrl.auth.guard :as guard])
  (:import (org.postgresql.util PGInterval)))
(use 'korma.core)

(defn notification-options-page [email]
  ; NOTE: should really figure out current value (if logged in) and pre-select the correct radio button. Currently it just hard codes values in the HTML ignoring current setting
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
  (update db/users
          (set-fields {:notification_email_interval (key-to-interval notification-interval)
                       :inbox_email_interval        (key-to-interval inbox-interval)})
          (where {:id (user :id)}))
  (layout/render "users/updated.html" {:message "Your notification preferences have been updated."}))

(defroutes preference-routes
           (GET "/notification-options" [email] (notification-options-page email))
           (POST "/blacklist-email" [email-for-blacklist] (blacklist-email email-for-blacklist))
           (POST "/update-notification-preferences" [notification-interval inbox-interval :as req] (guard/requires-login #(update-notification-preferences (session-from req) notification-interval inbox-interval))))
