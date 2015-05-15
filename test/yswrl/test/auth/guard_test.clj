(ns yswrl.test.auth.guard-test
  (:require [yswrl.auth.guard :as guard])
  (:use clojure.test))

(deftest guard-test
  (testing "Can tell you if a username doesn't exist"
    (is (not (guard/is-logged-in? {}))))

  (testing "Can tell you if a username doesn't exist"
    (is (guard/is-logged-in? { :session { :user { :username "Blah" }}})))

  )