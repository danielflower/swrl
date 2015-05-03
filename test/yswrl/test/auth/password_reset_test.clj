(ns yswrl.test.auth.password_reset_test
  (:require [yswrl.test.html-assert :refer :all]
            [yswrl.auth.password-reset :as pr]
            [yswrl.auth.auth-routes :as auth])
  (:use clojure.test
        ring.mock.request
        yswrl.handler
        ring.middleware.anti-forgery))

(defn now [] (System/currentTimeMillis))

(deftest forgotten-password
  (let [username (str "test-user-" (now))
        email (str username "@example.org")
        password "Abcd1234"
        req {}
        response (auth/handle-registration {:username username :email email :password password :confirmPassword password} req)
        user (:user (:session response))
        user-id (:id user)]

    (testing "An email with the user's name and link to reset can be created"
      (let [html (pr/create-forgotten-email-body "canman" "atokenofmyappreciation")]
        (is (.contains html "Dear canman,") html)
        (is (.contains html "http://www.youshouldwatchreadlisten.com/reset-password?token=atokenofmyappreciation") html)))

    (testing "A password for a user can be reset"
      (let [failed-attempt (auth/get-user-by-name-and-password username "HelloWorld")
            token (pr/create-reset-token)]
        (is (nil? failed-attempt))
        (pr/create-password-reset-request user-id (:hashed token))
        (pr/handle-reset-password (:unhashed token) "HelloWorld" {})
        (let [successful-attempt (auth/get-user-by-name-and-password username "HelloWorld")]
          (is (= user-id (:id successful-attempt)))))))

  (testing "Token hashing is determinstic"
    (let [one (pr/hash-token "agreattoken")
          two (pr/hash-token "agreattoken")]
      (is (= one two))))

  )