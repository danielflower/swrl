(ns yswrl.test.handler-test
  (:use clojure.test
        ring.mock.request
        yswrl.handler))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))
      (is (= "default-src 'self'; img-src *; frame-src *; child-src *; style-src 'self' 'unsafe-inline'; script-src 'self' www.google-analytics.com http://platform.twitter.com/widgets.js https://connect.facebook.net/en_GB/sdk.js http://connect.facebook.net/en_GB/sdk.js" ((:headers response) "Content-Security-Policy")))
      (is (= "0; mode=block" ((:headers response) "X-XSS-Protection")))
      (is (= "private, no-transform" ((:headers response) "Cache-Control")))
    ))

  (testing "immutable folder likes big caches and you know it can't lie"
    (let [response (app (request :get "/immutable/images/swirl-logo-v3.svg"))]
      (is (= 200 (:status response)))
      (is (= nil ((:headers response) "Content-Security-Policy")))
      (is (= "public, max-age=31556926, no-transform" ((:headers response) "Cache-Control")))))

  (testing "public folder does not cache stuff"
    (let [response (app (request :get "/favicon.ico"))]
      (is (= 200 (:status response)))
      (is (= nil ((:headers response) "Content-Security-Policy")))
      (is (= nil ((:headers response) "Cache-Control")))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response))))))
