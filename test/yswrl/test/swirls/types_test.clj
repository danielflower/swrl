(ns yswrl.test.swirls.types_test
  (:use clojure.test)
  (:require [yswrl.swirls.types :as t]))

(deftest types-test
  (testing "Empty or unrecognised values are considered website"
    (is (= "website" (t/from-open-graph-type nil)))
    (is (= "website" (t/from-open-graph-type "")))
    (is (= "website" (t/from-open-graph-type " ")))
    (is (= "website" (t/from-open-graph-type "blah"))))

  (testing "video is video"
    (is (= "video" (t/from-open-graph-type "video"))))

  )
