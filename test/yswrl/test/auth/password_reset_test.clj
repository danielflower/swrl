(ns yswrl.test.auth.password_reset_test
  (:require [yswrl.test.html-assert :refer :all]
            [yswrl.auth.password-reset :as pr])
  (:use clojure.test
        ring.mock.request
        yswrl.handler
        ring.middleware.anti-forgery))

(defn now [] (System/currentTimeMillis))

(deftest forgotten-password
    (testing "An email with the user's name and link to reset can be created"
      (let [html (pr/create-forgotten-email-body "canman" "atokenofmyappreciation")]
           (is (.contains html "Dear canman,") html)
           (is (.contains html "http://www.youshouldwatchreadlisten.com/reset-password?token=atokenofmyappreciation") html))))