(ns yswrl.test.swirls.postman-test
  (:use clojure.test)
  (:require [yswrl.swirls.postman :as postman]))

(deftest test-app
  (testing "swirl URL filter can convert an ID to a full link"
    (let [html (postman/email-body "swirls/comment-notification-email.html" {:swirl_url "http://url" :swirl { :title "Hello world" }})]
      (is (.contains html "<title>New comment on Hello world</title>") html))))


