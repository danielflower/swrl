(ns yswrl.test.html-assert
  (:use clojure.test))

(defn contains-html [expected response]
  (is (.contains (:body response) expected) (str (:body response) "\ndoes not contain: " expected)))

