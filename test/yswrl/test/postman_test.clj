(ns yswrl.test.postman-test
  (:use clojure.test)
  (:require [yswrl.swirls.postman :as postman]
            [yswrl.test.scaffolding :as s]))

(deftest postman
  (testing "does not email people on the blacklist"
    (let [user (s/create-test-user)
          _ (postman/blacklist (user :email))
          result (postman/send-email (user :email) (user :username) "Test subject" "Body")]
      (is (= [{:email "",
               :status "error",
               :reject_reason "Email is blacklisted"}] result))))

  (testing "non-blacklisted people can be emailed"
    (let [user (s/create-test-user)
          result (postman/send-email (user :email) (user :username) "Test subject" "Body")]
      (is (not= [{:email "",
               :status "error",
               :reject_reason "Email is blacklisted"}] result)))))