(ns yswrl.test.auth.auth-routes-test
  (:require [yswrl.auth.auth-routes :as auth-routes]
            [yswrl.test.scaffolding :as s]
            [yswrl.links :as links]
            [clojure.data.json :as json]
            [yswrl.auth.guard :as guard]
            [clj-time.core :as time])
  (:use clojure.test))

(deftest auth-routes-test

  (testing "Redirects only allow relative URLs and defaults to inbox"
    (is (= (links/inbox) (auth-routes/redirect-url nil)))
    (is (= (links/inbox) (auth-routes/redirect-url "")))
    (is (= "/blah?mah=car" (auth-routes/redirect-url "/blah?mah=car")))
    (is (= (links/inbox) (auth-routes/redirect-url "http://evil.site.com"))))

  (testing "Login can be view username or email"
    (let [user (s/create-test-user)]
      (is (= (user :id)
             (get-in (auth-routes/attempt-login (user :username) s/test-user-password false "/" {}) [:session :user :id])))
      (is (= (user :id)
             (get-in (auth-routes/attempt-login (user :email) s/test-user-password false "/" {}) [:session :user :id])))))

  (testing "Can login with app route"
    (let [user (s/create-test-user)
          response (auth-routes/attempt-app-login (:username user) s/test-user-password)
          response2 (auth-routes/attempt-app-login (:username user) s/test-user-password)
          body (:body response)
          json-body (json/read-str body)
          guard-check ((guard/requires-app-auth-token (fn [] :ok)) {:params {:user_id (:id user) :auth_token (get json-body "auth_token")}})]
      (is (= 200 (:status response)))
      (is (= (:id user) (get json-body "user_id")))
      (is (string? (get json-body "auth_token")))
      (is (= :ok guard-check))
      (is (= response response2))
      ))

  (testing "Can register with app route"
    (let [response (auth-routes/handle-app-registration {:username        (str "app_user-" (time/now))
                                                         :email           (str "app_user-" (time/now) "@test.com")
                                                         :password        "password"
                                                         :confirmPassword "password"}
                                                        auth-routes/password-hash-options)
          body (:body response)
          json-body (json/read-str body)
          guard-check ((guard/requires-app-auth-token (fn [] :ok)) {:params {:user_id (get json-body "user_id") :auth_token (get json-body "auth_token")}})]
      (is (= 200 (:status response)))
      (is (= :ok guard-check))
      ))

  (testing "Get correct error codes for app logins"
    (let [user (s/create-test-user)
          response-bad-login (auth-routes/attempt-app-login (:username user) "not the password")
          response-malformed-login (auth-routes/attempt-app-login nil nil)
          body-bad-login (json/read-str (:body response-bad-login))
          body-malformed-login (json/read-str (:body response-malformed-login))]
      (is (= 401 (:status response-bad-login)))
      (is (= 500 (:status response-malformed-login)))
      (is (string? (get body-bad-login "message")))
      (is (string? (get body-malformed-login "message")))
      ))
  (testing "Get correct errors when registering with app route"
    (let [bad-password (auth-routes/handle-app-registration {:username        (str "app_user-" (time/now))
                                                             :email           (str "app_user-" (time/now) "@test.com")
                                                             :password        "short"
                                                             :confirmPassword "short"}
                                                            auth-routes/password-hash-options)
          bad-password-body (json/read-str (:body bad-password))
          bad-confirmpassword (auth-routes/handle-app-registration {:username        (str "app_user2-" (time/now))
                                                                    :email           (str "app_user2-" (time/now) "@test.com")
                                                                    :password        "password"
                                                                    :confirmPassword "passwort"}
                                                                   auth-routes/password-hash-options)
          bad-confirmpassword-body (json/read-str (:body bad-confirmpassword))]
      (is (string?  (get bad-password-body "message")))
      (is (string? (get bad-confirmpassword-body "message")))
      (is (= 400 (:status bad-password)))
      (is (= 400 (:status bad-confirmpassword)))
      )))
