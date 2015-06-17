(ns yswrl.test.auth.password_reset_test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.auth.password-reset :as pr]
            [yswrl.auth.auth-routes :as auth]
            [yswrl.db :as db]
            [yswrl.links :as links])
  (:use clojure.test
        ring.mock.request
        yswrl.handler
        ring.middleware.anti-forgery))

(deftest forgotten-password
  (let [username (str "test-user-" (now))
        email (str username "@example.org")
        password "Abcd1234"
        req {}
        response (auth/handle-registration {:username username :email email :password password :confirmPassword password} req nil {:algorithm :sha256})
        user (:user (:session response))
        user-id (:id user)]

    (testing "An email with the user's name and link to reset can be created"
      (let [html (pr/create-forgotten-email-body "canman" "atokenofmyappreciation")]
        (is (.contains html "Dear canman,") html)
        (is (.contains html (links/absolute "/reset-password?token=atokenofmyappreciation")) html)))

    (testing "A password for a user can be reset"
      (let [failed-attempt (auth/get-user-by-username-or-email-and-password username "HelloWorld")
            token (pr/create-reset-token)]
        (is (nil? failed-attempt))
        (pr/create-password-reset-request user-id (:hashed token))
        (pr/handle-reset-password (:unhashed token) "HelloWorld" {} {:algorithm :sha256})
        (let [successful-attempt (auth/get-user-by-username-or-email-and-password username "HelloWorld")]
          (is (= user-id (:id successful-attempt))))))

    (testing "used tokens cannot be re-used"
      (let [token (pr/create-reset-token)
            _ (pr/create-password-reset-request user-id (:hashed token))
            _ (pr/handle-reset-password (:unhashed token) "HelloWorld" {} {:algorithm :sha256})
            _ (pr/handle-reset-password (:unhashed token) "WontBeChanged" {} {:algorithm :sha256})
            successful-attempt (auth/get-user-by-username-or-email-and-password username "HelloWorld")]
        (is (= user-id (:id successful-attempt)))))

    (testing "tokens over 24 hours old cannot be used"
      (let [token (pr/create-reset-token)]
        (pr/create-password-reset-request user-id (:hashed token))
        (db/execute "UPDATE password_reset_requests SET date_requested = date_requested + INTERVAL '-25 hours' WHERE hashed_token = ?", (:hashed token))
        (pr/handle-reset-password (:unhashed token) "HelloWorldo" {} {:algorithm :sha256})
        (is (nil? (auth/get-user-by-username-or-email-and-password username "HelloWorldo"))))))

  (testing "Token hashing is determinstic"
    (let [one (pr/hash-token "agreattoken")
          two (pr/hash-token "agreattoken")]
      (is (= one two)))))