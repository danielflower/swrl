(ns yswrl.test.swirls.swirl-links-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.swirl-links :as l])
  (:use clojure.test))

(deftest swirl-links-test

  (testing "Types can be gotten from strings"
    (is (= l/itunes-id (l/link-type-of "I")))
    ))