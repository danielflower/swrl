(ns yswrl.test.handler
  (:use clojure.test
        ring.mock.request
        yswrl.handler))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))
      (is (= "default-src 'self'; img-src *; frame-src *; child-src *" ((:headers response) "Content-Security-Policy")))
      (is (= nil ((:headers response) "Cache-Control")))))

  (testing "immutable folder likes big caches and you know it can't lie"
    (let [response (app (request :get "/immutable/css/skeleton-2.0.4.css"))]
      (is (= 200 (:status response)))
      (is (= nil ((:headers response) "Content-Security-Policy")))
      (is (= "max-age=31556926" ((:headers response) "Cache-Control")))))

  (testing "public folder does not cache stuff"
    (let [response (app (request :get "/css/screen.v3.css"))]
      (is (= 200 (:status response)))
      (is (= nil ((:headers response) "Content-Security-Policy")))
      (is (= nil ((:headers response) "Cache-Control")))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response))))))
