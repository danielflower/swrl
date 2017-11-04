(ns yswrl.auth.auth-routes
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.tools.logging :as log]
            [buddy.hashers :as hashers]
            [yswrl.auth.auth-repo :as users]
            [clojure.string :refer [trim]]
            [ring.util.response :refer [redirect response]]
            [yswrl.constraints :refer [max-length]]
            [yswrl.links :as links]
            [yswrl.rest.utils :as utils]
            [yswrl.auth.guard :as guard]
            [clojure.string :as string])
  (:import (java.sql BatchUpdateException)))

(def password-hash-options {:algorithm :bcrypt+sha512})

(defn registration-page [map]
  (layout/render "auth/register.html" map))

(defn login-page [& {:keys [username error return-url fb-errors error-message]}]
  (layout/render "auth/login.html" {:logister-info {:login-username username} :login-error error :return-url return-url
                                    :fb-errors     fb-errors :error-message error-message}))

(defn logged-out-page []
  (layout/render "auth/logged-out.html"))



(defn edit-profile-page [errors]
  (layout/render "users/edit-profile.html" {:title "Update your profile" :errors errors}))



(defn handle-logout [{session :session}]
  (let [newSession (dissoc session :user)]
    (->
      (redirect "/logged-out")
      (assoc :session newSession))))


(defn months [x] (* x 2419200))

(defn redirect-url [return-url]
  (if (or (clojure.string/blank? return-url) (not (.startsWith return-url "/")))
    (links/inbox)
    return-url))

(defn login-success [user remember-me? return-url req]
  (let [user-cookie {:id (user :id) :username (user :username) :email_md5 (user :email_md5) :email (user :email)}
        newSession (assoc (req :session) :user user-cookie)
        response (redirect (redirect-url return-url))]
    (if (true? remember-me?)
      (->
        response
        (assoc :session newSession :session-cookie-attrs {:max-age (months 3)}))
      (->
        response
        (assoc :session newSession)))))


(defn handle-update-profile [req user new-username new-email]
  (let [errors (first (b/validate user {:username [v/required [v/max-count (max-length :users :username)]]
                                        :email    [v/required [v/max-count (max-length :users :email)] [v/email :message "Please enter a valid email address"]]}))]

    (if errors
      (edit-profile-page errors)
      (do
        (try
          (try
            (users/update-user (user :id) new-username new-email)
            (login-success (users/get-user-by-id (user :id)) false (links/user new-username) req)
            (catch BatchUpdateException bue
              (throw (.getNextException bue))))
          (catch Exception e
            (let [message (cond
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_username_key\"") {:username '("A user with that username already exists. Please select a different username.")}
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_email_key\"") {:email '("A user with that email already exists. Please select a different email.")}
                            :else {:unknown '("There was an unexpected error. Please try again later.")})
                  password-redacted-user (dissoc user :password :confirmPassword)]
              (log/warn "Error while updating user details. New username is " new-username " and new-email is " new-email " and old details were " password-redacted-user " and error was " e)
              (edit-profile-page message))))))))


(defn get-user-by-username-or-email-and-password [username-or-email password]
  (let [user-attempt-1 (users/get-user username-or-email)
        user (if (nil? user-attempt-1) (users/get-user-by-email username-or-email) user-attempt-1)]
    (if (and user (hashers/check password (:password user)))
      user
      nil)))

(defn attempt-login [username-or-email password remember-me? return-url req]
  (if-let [user (get-user-by-username-or-email-and-password username-or-email password)]
    (login-success user remember-me? return-url req)
    (login-page :username username-or-email :error true :return-url return-url)))


(defn hash-password [unhashed options]
  (hashers/encrypt unhashed options))

(defn handle-registration [user req return-url hash-options]
  (let [errors (first (b/validate user {:username        [v/required [v/max-count (max-length :users :username)]]
                                        :email           [v/required [v/max-count (max-length :users :email)] [v/email :message "Please enter a valid email address"]]
                                        :password        [v/required [v/min-count 8]]
                                        :confirmPassword [[(fn [confirmed] (or (nil? confirmed) (= confirmed (user :password)))) :message "Your passwords did not match"]]}))]
    (if errors
      (do
        (log/info "validation error on registration page" errors (user :email))
        (registration-page (assoc user :errors errors)))
      (do
        (try
          (let [created-user (users/create-user (user :username) (user :email) (hash-password (user :password) hash-options))]
            (users/migrate-suggestions-from-email (created-user :id) (created-user :email)))
          (log/info "New user registration klaxon:" (user :username) "-" (user :email))
          (attempt-login (user :username) (user :password) false return-url req)
          (catch Exception e
            (let [message (cond
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_username_key\"") {:username '("A user with that username already exists. Please select a different username.")}
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_email_key\"") {:email '("A user with that email already exists. Please select a different email, or log in if you already have an account.")}
                            :else {:unknown '("There was an unexpected error. Please try again later.")})
                  password-redacted-user (dissoc user :password :confirmPassword)]
              (if (not (nil? (get message :unknown)))
                (log/error "Error while registering user" password-redacted-user e)
                (log/info "User tried to register user that already exists" password-redacted-user message))
              (registration-page (assoc user :errors message)))))))))

(defn fixed-length-password
  ([] (fixed-length-password 8))
  ([n]
   (let [chars (map char (range 33 127))
         password (take n (repeatedly #(rand-nth chars)))]
     (reduce str password))))

(defn attempt-thirdparty-login [username email id return-url req]
  (if-let [user (users/get-user-by-email email)]
    (do
      (try (users/update-thirdparty-id (:id user) (:id_type id) (Long/parseLong (:id id)))
           (catch Exception e
             (throw (.getNextException e))))
      (login-success user true return-url req))
    (do
      (let [random-password (fixed-length-password 10)]
        (handle-registration {:username username :email email :password random-password :confirmPassword random-password}
                             req return-url password-hash-options)
        (let [user (users/get-user username)]
          (try (users/update-thirdparty-id (:id user) (:id_type id) (Long/parseLong (:id id)))
               (catch Exception e
                 (throw (.getNextException e))))
          (users/update-avatar-type (:id user) (:avatar_type id))
          (login-success user true return-url req))))))


(defn session-from [req] (:user (:session req)))

(defn attempt-app-login [username password]
  (try (if-let [user (get-user-by-username-or-email-and-password username password)]
         (if-let [auth-token (users/get-app-auth-token-for-user user)]
           (utils/json-response {:auth_token auth-token
                                 :user_id    (:id user)})
           (try
             (if-let [auth-token (users/generate-app-auth-token-for-user user)]
               (utils/json-response {:auth_token auth-token
                                     :user_id    (:id user)})
               (do (log/error "Failed to generate app-auth-token for user")
                   (utils/json-response {:message "Failed to generate app-auth-token for user"} 500)))
             (catch Exception e
               (log/error e "Failed to generate app-auth-token for user")
               (utils/json-response {:message "Failed to generate app-auth-token for user"} 500))))
         (utils/json-response {:message "Invalid Username/Password"} 401))
       (catch Exception e
         (log/error e "Unknown error occurred attempting to log in user " username)
         (utils/json-response {:message "Unknown error occurred attempting to log in user "} 500))))

(defn handle-app-registration [user hash-options]
  (let [errors (first (b/validate user {:username        [v/required [v/max-count (max-length :users :username)]]
                                        :email           [v/required [v/max-count (max-length :users :email)] [v/email :message "Please enter a valid email address"]]
                                        :password        [v/required [v/min-count 8]]
                                        :confirmPassword [[(fn [confirmed] (or (nil? confirmed) (= confirmed (user :password)))) :message "Your passwords did not match"]]}))]
    (if errors
      (do
        (log/info "validation error on app registration" errors (user :email))
        (utils/json-response {:message (->> errors
                                            vals
                                            (map first)
                                            (string/join " "))} 400))
      (do
        (try
          (let [created-user (users/create-user (user :username) (user :email) (hash-password (user :password) hash-options))]
            (users/migrate-suggestions-from-email (created-user :id) (created-user :email)))
          (log/info "New user registration klaxon:" (user :username) "-" (user :email))
          (attempt-app-login (user :username) (user :password))
          (catch Exception e
            (let [message (cond
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_username_key\"") {:username '("A user with that username already exists. Please select a different username.")}
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_email_key\"") {:email '("A user with that email already exists. Please select a different email, or log in if you already have an account.")}
                            :else {:unknown '("There was an unexpected error. Please try again later.")})
                  password-redacted-user (dissoc user :password :confirmPassword)]
              (if (not (nil? (get message :unknown)))
                (log/error "Error while registering user" password-redacted-user e)
                (log/info "User tried to register user that already exists" password-redacted-user message))
              (utils/json-response {:message message} 500))))))))

(defroutes auth-api-routes
           (POST "/app-login" [username password] (attempt-app-login username password))
           (POST "/test-app" [] (guard/requires-app-auth-token (fn [] (utils/json-response {:hello "world"}))))
           (POST "/app-register" [username email password confirmPassword]
             (handle-app-registration {:username (trim username) :email (trim email) :password password :confirmPassword confirmPassword} password-hash-options))
           )
(defroutes auth-routes
           (GET "/login" [return-url] (login-page :return-url return-url))
           (POST "/login" [username password remember return-url :as req] (attempt-login username password (if (= "on" remember) true false) return-url req))

           (GET "/logout" [:as req] (handle-logout req))
           (GET "/logged-out" [_] (logged-out-page))

           (GET "/edit-profile" [] (guard/requires-login #(edit-profile-page nil)))
           (POST "/edit-profile" [username email :as req] (guard/requires-login #(handle-update-profile req (session-from req) username email)))

           (GET "/register" [_] (registration-page nil))
           (POST "/register" [username email password confirmPassword return-url :as req]
             (handle-registration {:username (trim username) :email (trim email) :password password :confirmPassword confirmPassword} req return-url password-hash-options)))
