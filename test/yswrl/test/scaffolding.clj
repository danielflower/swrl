(ns yswrl.test.scaffolding
  (:require [yswrl.auth.auth-routes :as auth]
            [yswrl.swirls.swirls-repo :as swirls-repo])
  (:use clojure.test))

(defn now [] (System/currentTimeMillis))

(def test-user-password "Abcd1234")

(defn create-test-user []
  (let [username (str "test-user-" (now))
        email (str username "@example.org")
        password test-user-password
        req {}
        response (auth/handle-registration {:username username :email email :password password :confirmPassword password} req {:algorithm :sha256})]
    (:user (:session response))))

(defn user-to-relation [user]
  {:user-id (user :id) :username (user :username)})

(defn equal-ignoring-order? [& colls]
  (apply = (map frequencies colls)))

(defn create-swirl [authorId title review recipientNames]
  (let [swirl (swirls-repo/save-draft-swirl authorId title review nil)]
    (swirls-repo/publish-swirl (swirl :id) authorId title review recipientNames)
    swirl))
