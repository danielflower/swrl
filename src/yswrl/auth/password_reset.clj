(ns yswrl.auth.password-reset
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [buddy.hashers :as hashers]
            [yswrl.auth.auth-repo :as users]
            [clojure.string :refer [trim]]
            [ring.util.response :refer [redirect response]]
            [yswrl.swirls.postman :as postman]))

(defn forgot-password-page [usernameOrEmail error]
  (layout/render "auth/forgot-password.html" {:usernameOrEmail usernameOrEmail :error error}))

(defn forgot-password-sent-page []
  (layout/render "auth/forgot-password-sent.html"))

(defn create-forgotten-email-body [username token]
  (postman/email-body "auth/password-reset-email.html" { :username username :token token }))

(defn request-password-reset-email [usernameOrEmail]
  (let [user (first (users/get-users-by-username_or_email [usernameOrEmail]))]
    (if user
      (let [unhashed-token (str (java.util.UUID/randomUUID))
            hashed-token (hashers/encrypt unhashed-token {:algorithm :sha256 :salt "salthylskjdflaskjdfkl"})
            row (users/create-password-reset-request (:id user) hashed-token)]
        (postman/send-email [{:email (:email user) :name (:username user)}] "Password reset request" (create-forgotten-email-body (user :username) unhashed-token))
        (redirect "forgot-password-sent"))
      (forgot-password-page usernameOrEmail "No user with that email or username was found. <a href=\"/register\">Click here to register</a>."))))


(defroutes password-reset-routes
           (GET "/forgot-password" [_] (forgot-password-page "" nil))
           (POST "/request-password-reset-email" [usernameOrEmail] (request-password-reset-email usernameOrEmail))
           (GET "/forgot-password-sent" [_] (forgot-password-sent-page)))