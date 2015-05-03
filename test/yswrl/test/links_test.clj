(ns yswrl.test.links-test
  (:use clojure.test)
  (:require [yswrl.links :as links]))

(deftest links
  (testing "swirl URL creation"
    (let [url (links/swirl 10)]
      (is (= "http://www.youshouldwatchreadlisten.com/swirls/10" url))))

  (testing "password reset link"
    (let [url (links/password-reset "ADSF%&")]
      (is (= "http://www.youshouldwatchreadlisten.com/reset-password?token=ADSF%25%26" url))))

  (testing "user links"
    (let [url (links/user "Dan & / co")]
      (is (= "http://www.youshouldwatchreadlisten.com/swirls/by/Dan+%26+%2F+co" url)))))



