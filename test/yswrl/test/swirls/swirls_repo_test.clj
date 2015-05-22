(ns yswrl.test.swirls.swirls-repo-test
  (:require [yswrl.test.scaffolding :as s]
            [yswrl.swirls.swirls-repo :as repo])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker))

(deftest swirls-repo-test

  (let [author (s/create-test-user)
        responder (s/create-test-user)
        non-responder (s/create-test-user)
        swirl (s/create-swirl "generic" (author :id) "Animals" "Yeah" [(responder :username) (non-responder :username) "nonuser@example.org"], {})
        _ (repo/create-response (swirl :id) "HOT" responder)]

    (testing "Responses can be gotten"
      (is (= [{:responder (responder :id),
               :email_md5 (responder :email_md5),
               :username  (responder :username),
               :summary   "HOT"}]
             (repo/get-swirl-responses (swirl :id)))))


    (testing "People who have not responded can be got"
      (is (= [{:email_md5 (non-responder :email_md5),
               :username  (non-responder :username)}] (repo/get-non-responders (swirl :id)))))))