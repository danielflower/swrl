(ns yswrl.test.auth.auth-routes-test
  (:require [yswrl.auth.auth-routes :as auth-routes])
  (:use clojure.test))

(deftest auth-routes-test

  (testing "Redirects only allow relative URLs"
    (is (= "/" (auth-routes/redirect-url nil)))
    (is (= "/" (auth-routes/redirect-url "")))
    (is (= "/blah?mah=car" (auth-routes/redirect-url "/blah?mah=car")))
    (is (= "/" (auth-routes/redirect-url "http://evil.site.com"))))

  )
