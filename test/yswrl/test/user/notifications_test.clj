(ns yswrl.test.user.notifications-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.user.notifications :as notifications])
  (:use clojure.test))

(deftest notifications

  (let [recipient (create-test-user)
        author (create-test-user)
        swirl (create-swirl "generic" (author :id) "Animals" "Yeah" [(recipient :username)])
        ]

    (testing "are generated for the recipient whenever a swirl is recommended to them"
      (let [notes (notifications/get-for-user (recipient :id))]
        (is (= 1 (count notes)))
        (let [note (first notes)]
          (is (= (swirl :id) (note :swirl_id)))
          (is (= (swirl :id) (note :subject_id)))
          (is (= (recipient :id) (note :target_user_id)))
          (is (= "Animals" (note :swirl-title)))
          (is (= notifications/recommendation (note :notification_type))))))))