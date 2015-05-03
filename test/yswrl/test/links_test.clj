(ns yswrl.test.links-test
  (:use clojure.test)
  (:require [yswrl.links :as links]))

(deftest links
  (testing "swirl URL creation"
    (let [url (links/swirl 10)]
      (is (= "http://www.youshouldwatchreadlisten.com/swirls/10" url))))

  (testing "user links"
    (let [url (links/user "Dan & / co")]
      (is (= "http://www.youshouldwatchreadlisten.com/swirls/by/Dan+%26+%2F+co" url)))))



