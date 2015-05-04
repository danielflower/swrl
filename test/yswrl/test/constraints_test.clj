(ns yswrl.test.constraints_test
  (:require [yswrl.constraints :as c])
  (:use clojure.test))

(deftest forgotten-password
    (testing "the maximum length of the username field is known"
        (is (= 50 (c/max-length :users :username)))))
