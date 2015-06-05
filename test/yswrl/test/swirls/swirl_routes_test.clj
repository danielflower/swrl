(ns yswrl.test.swirls.swirl-routes-test
  (:use clojure.test)
  (:require [yswrl.swirls.swirl-routes :refer [usernames-and-emails-from-request]]))

(deftest swirl-routes-test
  (testing "posted usernames and passwords are parsed into a list"
    (is (= [] (usernames-and-emails-from-request nil nil)) "Nils are converted to empty list")
    (is (= [] (usernames-and-emails-from-request " " " ")) "Blanks are converted to empty list")
    (is (= ["dan1"] (usernames-and-emails-from-request "dan1" " ")) "A single checkbox name is converted to a list")
    (is (= ["dan1" "dan2"] (usernames-and-emails-from-request ["dan1" "dan2"] " ")) "An array of checkboxes is used directly")
    (is (= ["dan1"] (usernames-and-emails-from-request nil "dan1 ")) "If the only thing is a textbox value, then that is used")
    (is (= ["dan1" "dan2@example.org" "dan54"] (usernames-and-emails-from-request nil "dan1 , dan2@example.org ; dan54")) "The textbox can have comma-separated or semi-colon separated values")
    (is (= ["dan1" "dan2" "dan3"] (usernames-and-emails-from-request "dan1" "dan2,dan3")) "Checkboxes and textbox can be combined")
    (is (= ["dan1" "dan2" "dan3"] (usernames-and-emails-from-request ["dan1" "dan2"] "dan2,dan3 ; dan1 ")) "Duplicates are removed")
    ))
