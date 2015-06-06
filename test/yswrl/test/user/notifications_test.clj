(ns yswrl.test.user.notifications-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.user.notifications :as notifications])
  (:use clojure.test))

(deftest notifications

  (testing "are generated for the recipient whenever a swirl is recommended to them"
    (let [
          recipient (create-test-user)
          author (create-test-user)
          swirl (create-swirl "generic" (author :id) "Animals" "Yeah" [(recipient :username)])
          notes (notifications/get-for-user (recipient :id))
          ]
      (is (= 1 (count notes)))
      (let [note (first notes)]
        (is (= (swirl :id) (note :swirl_id)))
        (is (= (swirl :id) (note :subject_id)))
        (is (= (recipient :id) (note :target_user_id)))
        (is (= "Animals" (note :swirl-title)))
        (is (= notifications/recommendation (note :notification_type))))))

  (testing "a read notification is no longer returned"
    (let [
          recipient (create-test-user)
          author (create-test-user)
          swirl (create-swirl "generic" (author :id) "Aready read" "Meh" [(recipient :username)])
          _ (notifications/mark-as-seen (swirl :id) recipient)
          notes (notifications/get-for-user (recipient :id))
          ]
      (is (= 0 (count notes)))
      ))

  (testing "nothing happens if a swirl was already seen, or a random user that was never notified of the swirl or an anonymous user sees the swirl"
    (let [recipient (create-test-user)
          author (create-test-user)
          random-user (create-test-user)
          swirl (create-swirl "generic" (author :id) "Aready read" "Meh" [(recipient :username)])
          _ (notifications/mark-as-seen (swirl :id) recipient)]
      (is (= 0 (notifications/mark-as-seen (swirl :id) random-user)))
      (is (= 0 (notifications/mark-as-seen (swirl :id) nil)))
      (is (= 0 (notifications/mark-as-seen (swirl :id) recipient)))))

  )
