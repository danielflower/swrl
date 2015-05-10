(ns yswrl.auth.password-reset
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [buddy.hashers :as hashers]
            [yswrl.auth.auth-repo :as users]
            [yswrl.auth.auth-routes :refer [hash-password password-hash-options attempt-login]]
            [clojure.string :refer [trim]]
            [ring.util.response :refer [redirect response]]
            [yswrl.swirls.postman :as postman]
            [yswrl.db :as db]
            [clj-time.core :as t]
            [clj-time.coerce :as coerce]
            [yswrl.constraints :refer [max-length]]))
(use 'korma.core)

(defn forgot-password-page [usernameOrEmail error]
  (throw (NullPointerException. "Just testing error handling messages"))
  (layout/render "auth/forgot-password.html" {:usernameOrEmail usernameOrEmail :error error :maxUsernameOrEmailLength (max (max-length :users :email) (max-length :users :username))}))

(defn forgot-password-sent-page []
  (layout/render "auth/forgot-password-sent.html"))

(defn reset-password-page [token error]
  (layout/render "auth/reset-password.html" {:token token :error error}))

(defn create-password-reset-request [user-id, hashed-code]
  (insert db/password_reset_requests
          (values {:hashed_token hashed-code :user_id user-id})))

(defn create-forgotten-email-body [username token]
  (postman/email-body "auth/password-reset-email.html" {:username username :token token}))

(defn hash-token [token]
  (hashers/encrypt token {:algorithm :sha256 :salt "salthylskjdflaskjdfkl"}))

(defn create-reset-token []
  (let [unhashed (str (java.util.UUID/randomUUID))]
    {:unhashed unhashed
     :hashed   (hash-token unhashed)}))

(defn request-password-reset-email [usernameOrEmail]
  (let [user (first (users/get-users-by-username_or_email [usernameOrEmail]))]
    (if user
      (let [token (create-reset-token)]
        (create-password-reset-request (:id user) (:hashed token))
        (postman/send-email [{:email (:email user) :name (:username user)}] "Password reset request" (create-forgotten-email-body (user :username) (:unhashed token)))
        (redirect "/forgot-password-sent"))
      (forgot-password-page usernameOrEmail "No user with that email or username was found. <a href=\"/register\">Click here to register</a>."))))

(defn over-a-day-old [utc-date-time]
  (let [one-day-ago (t/minus (t/now) (t/hours 24))]
    (t/before? utc-date-time one-day-ago)))

(defn handle-reset-password [unhashed-token new-password req hash-options]
  (let [result (first (select db/password_reset_requests (where {:hashed_token (hash-token unhashed-token)})))]
    (if (or (nil? result) (over-a-day-old (coerce/from-sql-time (result :date_requested))))
      (reset-password-page nil "Sorry, that request was invalid. Please go to the login page and request a new password reset.")
      (let [user (users/get-user-by-id (:user_id result))]
        (users/change-password (:user_id result) (hash-password new-password hash-options))
        (delete db/password_reset_requests (where {:user_id (user :id)}))
        (attempt-login (:username user) new-password false nil req)))))

(defroutes password-reset-routes
           (GET "/forgot-password" [username] (forgot-password-page username nil))
           (POST "/forgot-password" [usernameOrEmail] (request-password-reset-email usernameOrEmail))
           (GET "/forgot-password-sent" [_] (forgot-password-sent-page))
           (GET "/reset-password" [token] (reset-password-page token nil))
           (POST "/reset-password" [token newPassword :as req] (handle-reset-password token newPassword req password-hash-options)))

