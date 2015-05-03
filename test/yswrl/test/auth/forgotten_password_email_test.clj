(ns yswrl.test.auth.forgotten_password_email_test
  (:require [yswrl.test.html-assert :refer :all]
            [yswrl.auth.auth-routes :as auth])
  (:use clojure.test
        ring.mock.request
        yswrl.handler
        ring.middleware.anti-forgery))

(defn now [] (System/currentTimeMillis))

(deftest forgotten-password
    (testing "An email with the user's name and link to reset can be created"
      (let [html (auth/create-forgotten-email-body "canman" "atokenofmyappreciation")]
           (is (.contains html "Dear canman,") html)
           (is (.contains html "http://www.youshouldwatchreadlisten.com/reset-password?token=atokenofmyappreciation") html))))