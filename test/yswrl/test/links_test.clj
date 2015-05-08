(ns yswrl.test.links-test
  (:use clojure.test)
  (:require [yswrl.links :as links]))

(deftest links
  (testing "swirl URL creation"
    (is (= "/swirls/10" (links/swirl 10))))

  (testing "password reset link"
    (is (= "/reset-password?token=ADSF%25%26" (links/password-reset "ADSF%&"))))

  (testing "user links"
    (is (= "/swirls/by/Dan+%26+%2F+co" (links/user "Dan & / co"))))

  (testing "absolute URLs"
    (is (= "http://www.youshouldwatchreadlisten.com/swirls/10" (links/absolute "/swirls/10")))))
