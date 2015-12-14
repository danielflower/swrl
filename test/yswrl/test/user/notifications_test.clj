(ns yswrl.test.user.notifications-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.swirl-routes :as swirl-routes]
            [yswrl.user.notifications :refer [recommendation new-response new-comment mark-as-seen mark-email-sent users-with-pending-notifications create-notification-email-body, clear-notifications-for-user-by-notification-type]]
            [yswrl.links :as links]
            [yswrl.user.notifications-repo :refer [get-for-user-email get-for-user-page unseen-notifications-count]]
            [yswrl.user.notifications :as notifications])
  (:use clojure.test)
  (:import (java.sql Timestamp)))

(defn contains-user [set-of-users user-to-find]
  (some (fn [u] (and
                  (> (u :id) 0)
                  (= (user-to-find :username) (u :username))
                  (= (user-to-find :email) (u :email))
                  (= (user-to-find :email_md5) (u :email_md5))))
        set-of-users))

(defn notification-types-for [user]
  (vec (map #(% :notification_type) (get-for-user-email (user :id)))))

(deftest notifications
  (testing "are generated for the recipient whenever a swirl is recommended to them"
    (let [recipient (create-test-user)
          author (create-test-user)
          swirl (create-swirl "website" (author :id) "Animals" "Yeah" [(recipient :username)])
          notes (get-for-user-email (recipient :id))
          ]
      (is (= 1 (count notes)))
      (let [note (first notes)]
        (is (= (swirl :id) (note :swirl_id)))
        (is (= (swirl :id) (note :subject_id)))
        (is (= (recipient :id) (note :target_user_id)))
        (is (= "Animals" (note :swirl-title)))
        (is (= recommendation (note :notification_type))))))

  (testing "are generated for the author when someone comments or responds to a swirl they authored"
    (let [responder (create-test-user)
          author (create-test-user)
          swirl (create-swirl "website" (author :id) "Something to respond to" "Yeah" [(responder :username)])
          _ (swirl-routes/handle-comment (swirl :id) "This is a comment" responder)
          _ (swirl-routes/handle-response (swirl :id) nil "Loved it" responder)
          notes (get-for-user-email (author :id))]
      (is (= 2 (count notes)) notes)
      (let [comment (first notes)]
        (is (= (swirl :id) (comment :swirl_id)))
        (is (= (author :id) (comment :target_user_id)))
        (is (= "Something to respond to" (comment :swirl-title)))
        (is (= (responder :username) (comment :instigator-username)))
        (is (= new-comment (comment :notification_type))))
      (let [response (nth notes 1)]
        (is (= (swirl :id) (response :swirl_id)))
        (is (= (author :id) (response :target_user_id)))
        (is (= (responder :username) (response :instigator-username)))
        (is (= "Loved it" (response :summary)))
        (is (= "Something to respond to" (response :swirl-title)))
        (is (= new-response (response :notification_type))))))

  (testing "someone on the suggestion list of a swirl is notified of comments and responses"
    (let [responder (create-test-user)
          suggested-user (create-test-user)
          suggested-user-that-has-seen-page (create-test-user)
          author (create-test-user)
          unrelated-bystander (create-test-user)
          swirl (create-swirl "website" (author :id) "Something to respond to" "Yeah" [(suggested-user :username) (responder :username) (suggested-user-that-has-seen-page :username)])
          _ (swirl-routes/handle-comment (swirl :id) "This is a comment" responder)
          _ (swirl-routes/handle-response (swirl :id) nil "Loved it" responder)
          _ (swirl-routes/handle-comment (swirl :id) "This is a response" author)
          _ (mark-as-seen (swirl :id) suggested-user-that-has-seen-page)]
      (is (= [recommendation new-comment new-response new-comment] (notification-types-for suggested-user)))
      (is (= [new-comment new-response] (notification-types-for author)))
      (is (= [] (notification-types-for unrelated-bystander)))
      (is (= [] (notification-types-for suggested-user-that-has-seen-page)))
      (is (= [recommendation new-comment] (notification-types-for responder)))))

  (testing "are not sent for certain responses such as Dismissed or Later"
    (let [responder (create-test-user)
          suggested-user (create-test-user)
          suggested-user-that-has-seen-page (create-test-user)
          author (create-test-user)
          unrelated-bystander (create-test-user)
          swirl (create-swirl "website" (author :id) "Something to respond to" "Yeah" [(suggested-user :username) (responder :username) (suggested-user-that-has-seen-page :username)])
          _ (swirl-routes/handle-response (swirl :id) nil "Dismissed" responder)
          _ (swirl-routes/handle-response (swirl :id) nil "Later" responder)
          _ (swirl-routes/handle-response (swirl :id) nil "Not for me" responder)]
      (is (= [recommendation] (notification-types-for suggested-user)) "user added by author should just see notification")
      (is (= [] (notification-types-for author)) "Author should have no notifications")
      (is (= [] (notification-types-for unrelated-bystander)) "unrelated people should have no notifications")
      (is (= [recommendation] (notification-types-for responder))) "the responder should have something... what?"))


  (testing "a read notification is no longer returned"
    (let [recipient (create-test-user)
          author (create-test-user)
          swirl (create-swirl "website" (author :id) "Aready read" "Meh" [(recipient :username)])
          _ (mark-as-seen (swirl :id) recipient)
          notes (get-for-user-email (recipient :id))
          ]
      (is (= 0 (count notes)))))


  (testing "a seen response is no longer returned"
    (let [recipient (create-test-user)
          responder (create-test-user)
          author (create-test-user)
          swirl (create-swirl "website" (author :id) "Aready read" "Meh" [(recipient :username) (responder :username)])
          _ (swirl-routes/handle-response (swirl :id) nil "Loved it" responder)
          before (get-for-user-page (recipient :id))]
      (is (= 2 (unseen-notifications-count (recipient :id)) (count (filter #(nil? (% :date_seen)) before))))
      (clear-notifications-for-user-by-notification-type (recipient :id) notifications/new-response)
      (is (= 1 (unseen-notifications-count (recipient :id)) (count (filter #(nil? (% :date_seen)) (get-for-user-page (recipient :id))))))))

  (testing "an emailed notification is no longer returned for emails, but is for the webpage"
    (let [recipient (create-test-user)
          author (create-test-user)
          _ (create-swirl "website" (author :id) "Aready read" "Meh" [(recipient :username)])
          _ (mark-email-sent recipient)
          for-email (get-for-user-email (recipient :id))
          for-page (get-for-user-page (recipient :id))
          ]
      (is (= 0 (count for-email)))
      (is (= 1 (count for-page)))))

  (testing "nothing happens if a swirl was already seen, or a random user that was never notified of the swirl or an anonymous user sees the swirl"
    (let [recipient (create-test-user)
          author (create-test-user)
          random-user (create-test-user)
          swirl (create-swirl "website" (author :id) "Aready read" "Meh" [(recipient :username)])
          _ (mark-as-seen (swirl :id) recipient)]
      (is (= 0 (mark-as-seen (swirl :id) random-user)))
      (is (= 0 (mark-as-seen (swirl :id) nil)))
      (is (= 0 (mark-as-seen (swirl :id) recipient)))))

  (testing "users that require notification"
    (let [recipient (create-test-user)
          author (create-test-user)
          seen-it (create-test-user)
          been-emailed (create-test-user)
          was-emailed-ages-ago (create-test-user)
          swirl (create-swirl "website" (author :id) "Aready read" "Meh" [(recipient :username)
                                                                          (seen-it :username)
                                                                          (been-emailed :username)
                                                                          (was-emailed-ages-ago :username)])
          _ (mark-as-seen (swirl :id) seen-it)
          _ (mark-email-sent been-emailed)
          actual-list (users-with-pending-notifications)
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
          _ (create-swirl "website" (author :id) "Some older swirl" "Meh" [(emailed-recently :username)
                                                                           (not-emailed-recently :username)])
          _ (mark-email-sent not-emailed-recently (Timestamp. 1420070400))
          _ (mark-email-sent emailed-recently)

          _ (create-swirl "website" (author :id) "Newer swirl" "Meh" [(emailed-recently :username)
                                                                      (not-emailed-recently :username)])

          actual-list (users-with-pending-notifications)
          actual (set actual-list)]
      (is (= (count actual-list) (count actual)))
      (is (contains-user actual not-emailed-recently))
      (is (not (contains-user actual emailed-recently)))))

  (testing "notification HTML groups by swirls and lists activity"
    (let [author (create-test-user)
          recipient (create-test-user)
          another-user (create-test-user)
          swirl1 (create-swirl "website" (author :id) "All Day" "Meh" [(recipient :username) (another-user :username)])
          _ (swirl-routes/handle-comment (swirl1 :id) "This is a comment" another-user)
          _ (swirl-routes/handle-response (swirl1 :id) nil "Loved it" another-user)
          _ (swirl-routes/handle-comment (swirl1 :id) "This is a response" author)
          _ (notifications/add notifications/added-to-group (recipient :id) nil 1 (author :id))
          swirl2 (create-swirl "website" (author :id) "Feed the animals" "Meh" [(recipient :username) (another-user :username)])
          html (create-notification-email-body recipient (get-for-user-email (recipient :id)))
          ]
      (is (.contains html (str "Dear " (recipient :username))) html)
      (is (.contains html (str "<a href=\"" (links/absolute (links/swirl (swirl1 :id))) "\">All Day</a>")) html)
      (is (.contains html (str (author :username) " recommended this to you")) html)
      (is (.contains html (str (another-user :username) " added a comment")) html)
      (is (.contains html (str (author :username) " added you to <a href=\"" (links/absolute (links/group "1")) "\">a new group</a>")) html)
      (is (.contains html (str (another-user :username) " responded <strong>Loved it</strong>")) html)
      (is (.contains html (str (author :username) " added a comment")) html)

      (is (.contains html (str "<a href=\"" (links/absolute (links/swirl (swirl2 :id))) "\">Feed the animals</a>")) html)
      (is (.contains html (str (author :username) " recommended this to you")) html)
      ))
  )
