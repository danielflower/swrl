(ns yswrl.test.auth.guard-test
  (:require [yswrl.auth.guard :as guard]
            [clj-time.core :as time]
            [yswrl.test.scaffolding :as s])
  (:use clojure.test))

(deftest guard-test
  (testing "Can tell you if a username doesn't exist"
    (is (not (guard/is-logged-in? {}))))

  (testing "Can tell you if a username doesn't exist"
    (is (guard/is-logged-in? { :session { :user { :username "Blah" }}})))

  (testing "Can verify a user's auth token"
    (let [user (s/create-test-user)
          auth-token (yswrl.auth.auth-repo/generate-app-auth-token-for-user user)]
      (is (= :ok
             ((guard/requires-app-auth-token (fn [] :ok)) {:params {:user_id (:id user) :auth_token auth-token}})))

      ;Requests can look like strings for ID
      (is (= :ok
             ((guard/requires-app-auth-token (fn [] :ok)) {:params {:user_id (str (:id user)) :auth_token auth-token}})))
      (is (= 403
             (:status ((guard/requires-app-auth-token (fn [] :ok)) {:params {:user_id (:id user) :auth_token "not the token"}}))))))
  )