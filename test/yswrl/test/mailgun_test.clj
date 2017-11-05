(ns yswrl.test.mailgun-test
  (:use clojure.test)
  (:require [yswrl.swirls.mailgun :as mailgun]
            [yswrl.test.scaffolding :as s]))

(deftest mailgun
  (testing "does not email people on the blacklist"
    (let [user (s/create-test-user)
          _ (mailgun/blacklist (user :email))
          result (mailgun/send-email (user :email) "Test subject" "Body")]
      (is (= [{:email "",
               :status "error",
               :reject_reason "Email is blacklisted"}] result))))

  (testing "non-blacklisted people can be emailed"
    (let [user (s/create-test-user)
          result (mailgun/send-email (user :email) "Test subject" "Body")]
      (is (not= [{:email "",
                  :status "error",
                  :reject_reason "Email is blacklisted"}] result)))))