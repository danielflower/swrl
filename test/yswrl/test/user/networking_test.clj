(ns yswrl.test.user.networking-test
  (:require [yswrl.test.html-assert :refer :all]
            [yswrl.auth.auth-routes :as auth]
            [yswrl.user.networking :as networking])
  (:use clojure.test))

(defn now [] (System/currentTimeMillis))

(defn create-test-user []
  (let [username (str "test-user-" (now))
        email (str username "@example.org")
        password "Abcd1234"
        req {}
        response (auth/handle-registration {:username username :email email :password password :confirmPassword password} req {:algorithm :sha256})]
    (:user (:session response))))

(deftest networking

  (let [user1 (create-test-user)
        user2 (create-test-user)
        user3 (create-test-user)
        ids-1-and-2 [(user1 :id) (user2 :id)]]

    (testing "A relation between two users can be created and changed"
      (networking/store (user1 :id) :knows (user2 :id))
      (is (= [(user2 :id)] (networking/get-relations (user1 :id) :knows)))
      (networking/store (user1 :id) :ignores (user2 :id))
      (is (empty? (networking/get-relations (user1 :id) :knows)))
      (is (= [(user2 :id)] (networking/get-relations (user1 :id) :ignores))))

    (testing "Multiple relations can be added together"
      (networking/store-multiple (user3 :id) :knows ids-1-and-2)
      (is (= ids-1-and-2 (networking/get-relations (user3 :id) :knows)))
      (networking/store-multiple (user3 :id) :ignores ids-1-and-2)
      (is (empty? (networking/get-relations (user3 :id) :knows)))
      (is (= ids-1-and-2 (networking/get-relations (user3 :id) :ignores))))

    ))