(ns yswrl.routes.auth
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [clojure.java.io :as io]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [buddy.hashers :as hashers]
            [yswrl.db.core :as db]
            [ring.util.response :refer [redirect response]]))





(defn registration-page []
  (layout/render "auth/register.html"))
(defn login-page [& {:keys [username]}]
  (layout/render "auth/login.html" {:username username}))
(defn logged-out-page []
  (layout/render "auth/logged-out.html"))


(defn handle-logout [{session :session}]
  (let [newSession (dissoc session :user)]
    (->
      (redirect "/logged-out")
      (assoc :session newSession)
      )

  ))

(defn login-success [user {session :session}]
  (let [newSession (assoc session :user user)]
    (->
      (redirect "/")
      (assoc :session newSession)
      )))

(defn attempt-login [username password req]
  (do
    (let [user (db/get-user username)]
      (if (and user (hashers/check password (:password user)))
        (login-success user req)
        (login-page :username username))
      )))


(defn handle-registration [username email password confirmPassword]
  (do
    (db/create-user username email (hashers/encrypt password))
    (redirect "/")))




(defroutes auth-routes
           (GET "/login" [_] (login-page))
           (POST "/login" [username password :as req] (attempt-login username password req))

           (GET "/logout" [ :as req] (handle-logout req))
           (GET "/logged-out" [_] (logged-out-page))

           (GET "/register" [_] (registration-page))
           (POST "/register" [username email password confirmPassword]
                 (handle-registration username email password confirmPassword))
           )
