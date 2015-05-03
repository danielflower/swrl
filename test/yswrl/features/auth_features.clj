(ns yswrl.features.auth-features
  (:require [yswrl.handler :refer [app]]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]))

(deftest homepage-greeting
  (-> (session app)
      (visit "/")
      (within [:h1]
              (has (text? "Welcome to yswrl")))))