(ns yswrl.routes.auth
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [clojure.java.io :as io]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [buddy.hashers :as hashers]
            [yswrl.db.core :as db]
            [ring.util.response :refer [redirect]]))





(defn registration-page []
  (layout/render "auth/register.html"))
(defn login-page [& {:keys [username] } ]
  (layout/render "auth/login.html" { :username username }))


(defn login-success [user session]
  (assoc session :user user)
  (redirect "/")
  )

(defn attempt-login [username password {session :session}]
  (do
    (let [user (db/get-user username)]
      (if (and user (hashers/check password (:password user)))
        (login-success user session)
        (login-page :username username ))
      )))


(defn handle-registration [username email password confirmPassword]
  (do
    (db/create-user username email (hashers/encrypt password))
    (redirect "/")))




(defroutes auth-routes
           (GET "/login" [_] (login-page))
           (POST "/login" [username password :as req] (attempt-login username password req))
           (GET "/register" [_] (registration-page))
           (POST "/register" [username email password confirmPassword]
                 (handle-registration username email password confirmPassword))
           )
