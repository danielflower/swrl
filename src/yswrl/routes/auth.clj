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



(defn login [username password]
  (do
    (let [user (db/get-user username)]
      (if (hashers/check password (:password user)) user nil)
      )))


(defn handle-registration [username email password confirmPassword]
  (do
    (db/create-user username email (hashers/encrypt password))
    (redirect "/")))




(defroutes auth-routes
           (GET "/register" [_] (registration-page))
           (POST "/register" [username email password confirmPassword]
                 (handle-registration username email password confirmPassword))
           )
