(ns yswrl.test.auth.auth-routes-test
  (:require [yswrl.auth.auth-routes :as auth-routes]
            [yswrl.links :as links])
  (:use clojure.test))

(deftest auth-routes-test

  (testing "Redirects only allow relative URLs and defaults to inbox"
    (is (= (links/inbox) (auth-routes/redirect-url nil)))
    (is (= (links/inbox) (auth-routes/redirect-url "")))
    (is (= "/blah?mah=car" (auth-routes/redirect-url "/blah?mah=car")))
    (is (= (links/inbox) (auth-routes/redirect-url "http://evil.site.com"))))

  )
