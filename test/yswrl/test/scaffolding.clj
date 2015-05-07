(ns yswrl.test.scaffolding
  (:require [yswrl.test.html-assert :refer :all]
            [yswrl.auth.auth-routes :as auth])
  (:use clojure.test))

(defn now [] (System/currentTimeMillis))

(defn create-test-user []
  (let [username (str "test-user-" (now))
        email (str username "@example.org")
        password "Abcd1234"
        req {}
        response (auth/handle-registration {:username username :email email :password password :confirmPassword password} req {:algorithm :sha256})]
    (:user (:session response))))
