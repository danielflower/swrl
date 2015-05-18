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
      [yswrl.constraints :refer [max-length]] [yswrl.auth.auth-repo :as user]))

(def password-hash-options {:algorithm :bcrypt+sha512 })

(defn registration-page [map]
  (layout/render "auth/register.html" map))

(defn login-page [& {:keys [username error return-url]}]
  (layout/render "auth/login.html" {:username username :error error :return-url return-url}))

(defn logged-out-page []
  (layout/render "auth/logged-out.html"))


(defn handle-logout [{session :session}]
  (let [newSession (dissoc session :user)]
    (->
      (redirect "/logged-out")
      (assoc :session newSession)
      )))

(defn months [x] (* x 2419200))

(defn redirect-url [return-url]
  (if (or (clojure.string/blank? return-url) (not (.startsWith return-url "/")))
    "/"
    return-url))

(defn login-success [user remember-me? return-url req]
  (let [newSession (assoc (req :session) :user user)
        response (redirect (redirect-url return-url))]
    (if (true? remember-me?)
      (->
        response
        (assoc :session newSession :session-cookie-attrs {:max-age (months 3)}))
      (->
        response
        (assoc :session newSession)))))

(defn get-user-by-name-and-password [username password]
  (let [user (users/get-user username)]
    (if (and user (hashers/check password (:password user)))
      user
      nil)))

(defn attempt-login [username password remember-me? return-url req]
  (if-let [user (get-user-by-name-and-password username password)]
    (login-success user remember-me? return-url req)
    (login-page :username username :error true :return-url return-url)))


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
          (users/create-user (user :username) (user :email) (hash-password (user :password) hash-options))
          (attempt-login (user :username) (user :password) false return-url req)
          (catch Exception e
            (let [message (cond
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_username_key\"") {:username '("A user with that username already exists. Please select a different username.")}
                            (.contains (.getMessage e) "duplicate key value violates unique constraint \"users_email_key\"") {:email '("A user with that email already exists. Please select a different email, or log in if you already have an account.")}
                            :else {:unknown '("There was an unexpected error. Please try again later.")})]
              (log/error "Error while registering user" user e)
              (registration-page (assoc user :errors message)))))))))

(defn attempt-thirdparty-login [username email return-url req]
      (if-let [user (users/get-user-by-email email)]
          (login-success user true return-url req)
          (do
              (handle-registration {:username username :email email :password "shouldrandomise" :confirmPassword "shouldrandomise"}
                                   req return-url password-hash-options)
              (login-success (users/get-user username) true return-url req)
              )))

(defroutes auth-routes
           (GET "/login" [return-url] (login-page :return-url return-url))
           (POST "/login" [username password remember return-url :as req] (attempt-login username password (if (= "on" remember) true false) return-url req))

           (GET "/logout" [:as req] (handle-logout req))
           (GET "/logged-out" [_] (logged-out-page))

           (GET "/register" [_] (registration-page nil))
           (POST "/register" [username email password confirmPassword return-url :as req]
             (handle-registration {:username (trim username) :email (trim email) :password password :confirmPassword confirmPassword} req return-url password-hash-options)))