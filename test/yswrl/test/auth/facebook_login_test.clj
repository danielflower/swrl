(ns yswrl.test.auth.facebook-login-test
  (:require [yswrl.auth.facebook-login :as facebook-login])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker))


(def APP_ID (or (System/getenv "FACEBOOK_APP_ID")
                "894319820613855"))
(def APP_SECRET (or (System/getenv "FACEBOOK_APP_SECRET")
                    "b3ef0cc194e2abcfacd1ba32b085da2f"))

(deftest facebook-login-test

  (testing "oauth parameters are generated correctly"
    (let [req {:scheme ":http" :server-port 6000 :server-name "my_domain" :someother "token" :session "some_session"}]
      (is (= {:authorization-uri "https://graph.facebook.com/oauth/authorize"
            :access-token-uri  "https://graph.facebook.com/oauth/access_token"
            :redirect-uri      "http://my_domain:6000/facebook_auth"
            :client-id APP_ID
            :client-secret APP_SECRET
            :access-query-param :access_token
            :scope ["email"]
            :response_type "code"
            :grant-type "authorization_code"} (facebook-login/facebook-oauth2 req)))))

  (testing "getting facebook code works when exists"
    (let [req {:query-params {"code" "abcde" "another" "huh"}}]
      (is (= "abcde" (facebook-login/get-facebook-code req)))))

  (testing "no facebook code in req returns nil"
    (let [req {}]
      (is (= nil (facebook-login/get-facebook-code req))))
    (let [req {:query-params {"no-code" "true"}}]
      (is (= nil (facebook-login/get-facebook-code req))))
  )

  (testing "Can get facebook access token"
    (let [req {:scheme ":http" :server-port 6000 :server-name "my_domain" :someother "token" :session "some_session"}
          code "abcde"]
      (with-faked-responses
        (is (= "CAAMtYQ85KN8BAKV3yyrXof7tRSMtw9JP62rx0y6MlLcsZBFBok7laFFgIyO0w8pyGQsDdZAVeZAQQDAnT1G5uDnaYeRJNy1678F4mxuYV30Qw4s4Q8EePRCcQKPnHfClUmzeDB8FlbjFTyAQqt59HI3yfvKlkWjtFJN8cZCRFXZBUchimsre7lLOPMJVfb8sJOS1W6qJMgpDEA8zza6VB"
               (facebook-login/get-facebook-access-token code req))))))

  (testing "returns nil when response has no token"
    (let [req {:scheme ":http" :server-port 6000 :server-name "my_domain" :someother "token" :session "some_session"}
          code "nocode"]
      (with-faked-responses
        (is (= nil
               (facebook-login/get-facebook-access-token code req))))))

  (testing "gets the correct facebook user details"
    (let [access-token "wxyz"]
      (with-faked-responses
        (let [user-details (facebook-login/get-facebook-user-details access-token)]
          (is (= "Kyle Harrison" (get user-details "name")))
          (is (= "kylejharrison@gmail.com" (get user-details "email")))))))

  (testing "returns nil when no facebook user details"
    (let [access-token "nocode"]
      (with-faked-responses
        (let [user-details (facebook-login/get-facebook-user-details access-token)]
          (is (= nil (get user-details "name")))
          (is (= nil (get user-details "email")))))))
)


