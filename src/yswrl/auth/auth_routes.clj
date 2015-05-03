(ns yswrl.auth.auth-routes
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [taoensso.timbre :as log]
            [buddy.hashers :as hashers]
            [yswrl.auth.auth-repo :as users]
            [clojure.string :refer [trim]]
            [ring.util.response :refer [redirect response]]))


(defn registration-page [map]
  (layout/render "auth/register.html" map))

(defn login-page [& {:keys [username error]}]
  (layout/render "auth/login.html" {:username username :error error}))

(defn forgot-password-page [usernameOrEmail error]
  (layout/render "auth/forgot-password.html" {:usernameOrEmail usernameOrEmail :error error}))
(defn forgot-password-sent-page []
  (layout/render "auth/forgot-password-sent.html"))


(defn logged-out-page []
  (layout/render "auth/logged-out.html"))


(defn handle-logout [{session :session}]
  (let [newSession (dissoc session :user)]
    (->
      (redirect "/logged-out")
      (assoc :session newSession)
      )))

(defn months [x] (* x 2419200))

(defn login-success [user remember-me? {session :session}]
  (let [newSession (assoc session :user user)
        response (redirect "/")]
    (if (true? remember-me?)
      (->
        response
        (assoc :session newSession :session-cookie-attrs {:max-age (months 3)}))
      (->
        response
        (assoc :session newSession)))))

(defn attempt-login [username password remember-me? req]
  (let [user (users/get-user username)]
    (if (and user (hashers/check password (:password user)))
      (login-success user remember-me? req)
      (login-page :username username :error true))))

(defn handle-registration [user req]
  (let [errors (first (b/validate user {:username        [v/required [v/max-count 50]]
                                        :email           [v/required [v/max-count 100] [v/email :message "Please enter a valid email address"]]
                                        :password        [v/required [v/min-count 8]]
                                        :confirmPassword [v/required [(fn [confirmed] (= confirmed (user :password))) :message "Your passwords did not match"]]}))]
    (if errors
      (do
        (log/info "validation error on registration page" errors)
        (registration-page (assoc user :errors errors)))
      (do
        (try
          (users/create-user (user :username) (user :email) (hashers/encrypt (user :password)))
          (attempt-login (user :username) (user :password) false req)
          (catch Exception e
            (let [message (cond
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_username_key\"") {:username '("A user with that username already exists. Please select a different username.")}
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_email_key\"") {:email '("A user with that email already exists. Please select a different email, or log in if you already have an account.")}
                            :else {:unknown '("There was an unexpected error. Please try again later.")})]
              (log/error "Error while registering user" user e)
              (registration-page (assoc user :errors message)))))))))

(defn create-forgotten-email-body [username token]
  (yswrl.swirls.postman/email-body "auth/password-reset-email.html" { :username username :token token }))

(defn request-password-reset-email [usernameOrEmail]
  (let [user (first (users/get-users-by-username_or_email [usernameOrEmail]))]
    (if user
      (let [unhashed-token (str (java.util.UUID/randomUUID))
            hashed-token (hashers/encrypt unhashed-token {:algorithm :sha256 :salt "salthylskjdflaskjdfkl"})
            row (users/create-password-reset-request (:id user) hashed-token)]
        (yswrl.swirls.postman/send-email [{:email (:email user) :name (:username user)}] "Password reset request" (create-forgotten-email-body (user :username) unhashed-token))
        (redirect "forgot-password-sent"))
      (forgot-password-page usernameOrEmail "No user with that email or username was found. <a href=\"/register\">Click here to register</a>."))))

(defroutes auth-routes
           (GET "/login" [_] (login-page))
           (POST "/login" [username password remember :as req] (attempt-login username password (if (= "on" remember) true false) req))

           (GET "/forgot-password" [_] (forgot-password-page "" nil))
           (POST "/request-password-reset-email" [usernameOrEmail] (request-password-reset-email usernameOrEmail))
           (GET "/forgot-password-sent" [_] (forgot-password-sent-page))

           (GET "/logout" [:as req] (handle-logout req))
           (GET "/logged-out" [_] (logged-out-page))

           (GET "/register" [_] (registration-page nil))
           (POST "/register" [username email password confirmPassword :as req]
             (handle-registration {:username (trim username) :email (trim email) :password password :confirmPassword confirmPassword} req))
           )