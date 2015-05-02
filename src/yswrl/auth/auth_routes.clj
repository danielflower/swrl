(ns yswrl.auth.auth-routes
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [clojure.java.io :as io]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [buddy.hashers :as hashers]
            [yswrl.auth.auth-repo :as users]
            [ring.util.response :refer [redirect response]]))


(defn registration-page []
  (layout/render "auth/register.html"))

(defn login-page [& {:keys [username error]}]
  (layout/render "auth/login.html" {:username username :error error}))

(defn logged-out-page []
  (layout/render "auth/logged-out.html"))


(defn handle-logout [{session :session}]
  (let [newSession (dissoc session :user)]
    (->
      (redirect "/logged-out")
      (assoc :session newSession)
      )

    ))

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
  (do
    (println "Remember me? " remember-me?)
    (let [user (users/get-user username)]
      (if (and user (hashers/check password (:password user)))
        (login-success user remember-me? req)
        (login-page :username username :error true))
      )))


(defn handle-registration [username email password confirmPassword req]
  (do
    (users/create-user username email (hashers/encrypt password))
    (attempt-login username password false req)))




(defroutes auth-routes
           (GET "/login" [_] (login-page))
           (POST "/login" [username password remember :as req] (attempt-login username password (if (= "on" remember) true false) req))

           (GET "/logout" [:as req] (handle-logout req))
           (GET "/logged-out" [_] (logged-out-page))

           (GET "/register" [_] (registration-page))
           (POST "/register" [username email password confirmPassword :as req]
             (handle-registration username email password confirmPassword req))
           )
