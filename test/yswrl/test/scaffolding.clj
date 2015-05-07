(ns yswrl.test.scaffolding
  (:require [yswrl.auth.auth-routes :as auth])
  (:use clojure.test))

(defn now [] (System/currentTimeMillis))

(defn create-test-user []
  (let [username (str "test-user-" (now))
        email (str username "@example.org")
        password "Abcd1234"
        req {}
        response (auth/handle-registration {:username username :email email :password password :confirmPassword password} req {:algorithm :sha256})]
    (:user (:session response))))

(defn user-to-relation [user]
  {:user-id (user :id) :username (user :username)})

(defn equal-ignoring-order? [& colls]
  (apply = (map frequencies colls)))