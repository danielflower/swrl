(ns yswrl.test.auth.auth-routes-test
  (:require [yswrl.test.html-assert :refer :all]
            [yswrl.auth.auth-routes :as auth]
            [yswrl.db :as db])
  (:use clojure.test
        ring.mock.request
        yswrl.handler
        ring.middleware.anti-forgery))
(use 'korma.db)
(use 'korma.core)

(defn now [] (System/currentTimeMillis))

(deftest auth-routes
  (let [username (str "test-user-" (now))
        email (str username "@example.org")
        password "Abcd1234"
        req {}
        response (auth/handle-registration {:username username :email email :password password :confirmPassword password} req)
        user (:user (:session response))
        user-id (:id user)]

    (testing "A forgotten password request can be made"
      (let [resp (auth/request-password-reset-email username)
            forgot (first (select db/password_reset_requests (where {:user_id user-id}) (order :date_requested :DESC)))]
        ; can't test much more as the token is hashed, and the unhashed version is only available in the email which we don't have access to
        (is (not (nil? forgot)))))))



