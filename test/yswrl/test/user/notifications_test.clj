(ns yswrl.test.user.notifications-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.user.notifications :as notifications]
            [yswrl.links :as links])
  (:use clojure.test)
  (:import (java.sql Timestamp)))

(defn contains-user [set-of-users user-to-find]
  (some (fn [u] (and
                  (> (u :id) 0)
                  (= (user-to-find :username) (u :username))
                  (= (user-to-find :email_md5) (u :email_md5))))
        set-of-users))

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

  (testing "users that require notification"
    (let [recipient (create-test-user)
          author (create-test-user)
          seen-it (create-test-user)
          been-emailed (create-test-user)
          was-emailed-ages-ago (create-test-user)
          swirl (create-swirl "generic" (author :id) "Aready read" "Meh" [(recipient :username)
                                                                          (seen-it :username)
                                                                          (been-emailed :username)
                                                                          (was-emailed-ages-ago :username)])
          _ (notifications/mark-as-seen (swirl :id) seen-it)
          _ (notifications/mark-email-sent been-emailed)
          actual-list (notifications/users-with-pending-notifications)
          actual (set actual-list)
          ]
      (is (= (count actual) (count actual-list)))
      (is (contains-user actual recipient))
      (is (not (contains-user actual author)))
      (is (not (contains-user actual seen-it)))
      (is (not (contains-user actual been-emailed)))
      )
    )

  (testing "if a user was emailed recently about anything they will not get another notification unless it has been longer than a day"
    (let [emailed-recently (create-test-user)
          not-emailed-recently (create-test-user)
          author (create-test-user)
          _ (create-swirl "generic" (author :id) "Some older swirl" "Meh" [(emailed-recently :username)
                                                                           (not-emailed-recently :username)])
          _ (notifications/mark-email-sent not-emailed-recently (Timestamp. 1420070400))
          _ (notifications/mark-email-sent emailed-recently)

          _ (create-swirl "generic" (author :id) "Newer swirl" "Meh" [(emailed-recently :username)
                                                                      (not-emailed-recently :username)])

          actual-list (notifications/users-with-pending-notifications)
          actual (set actual-list)]
      (is (= (count actual-list) (count actual)))
      (is (contains-user actual not-emailed-recently))
      (is (not (contains-user actual emailed-recently)))
      )
    )

  (testing "if a user was emailed recently about anything they will not get another notification unless it has been longer than a day"
    (let [author (create-test-user)
          recipient (create-test-user)
          swirl1 (create-swirl "generic" (author :id) "All Day" "Meh" [(recipient :username)])
          swirl2 (create-swirl "generic" (author :id) "Feed the animals" "Meh" [(recipient :username)])
          html (notifications/create-notification-email-body recipient (notifications/get-for-user (recipient :id)))
          ]
      (is (.contains html (str "Dear " (recipient :username))) html)
      (is (.contains html (swirl1 :title)) html)
      (is (.contains html (links/absolute (links/swirl (swirl1 :id)))) html)
      (is (.contains html (swirl2 :title)) html)
      ))
  )
