(ns yswrl.test.scaffolding
  (:require [yswrl.auth.auth-routes :as auth]
            [yswrl.swirls.swirls-repo :as swirls-repo])
  (:use clojure.test))

(def counter (atom 0))

(defn now [] (System/currentTimeMillis))

(def test-user-password "Abcd1234")

(defn create-test-user [ & {:keys [username] :or {username (str "test-user-" (now) "_" (swap! counter inc))}}]
  (let [email (str username "@example.org")
        password test-user-password
        req {}
        response (auth/handle-registration {:username username :email email :password password :confirmPassword password} req nil {:algorithm :sha256})]
    (:user (:session response))))

(defn user-to-relation [user]
  {:user-id (user :id) :username (user :username)})

(defn equal-ignoring-order? [& colls]
  (apply = (map frequencies colls)))

(defn create-swirl [authorId title review recipientNames & {:keys [itunes_collection_id]}]
  (let [swirl (swirls-repo/save-draft-swirl authorId title review nil)]
    (swirls-repo/publish-swirl (swirl :id) authorId title review recipientNames)
    swirl))
