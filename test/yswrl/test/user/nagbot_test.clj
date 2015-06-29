(ns yswrl.test.user.nagbot-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.user.nagbot :as nagbot]
            [yswrl.user.notifications :refer [recommendation new-response new-comment mark-as-seen mark-email-sent users-with-pending-notifications create-notification-email-body]]
            [yswrl.user.notifications-repo :refer [get-for-user-email get-for-user-page]]
            [yswrl.swirls.swirl-routes :as swirl-routes])
  (:use clojure.test))


(deftest nagging
  (testing "suggested but unanswered swirls are sent to the user"
    (let [recipient (create-test-user)
          author (create-test-user)
          emailed-user (create-test-user)
          responded (create-swirl "generic" (author :id) "Thing that was loved" "Yeah" [(recipient :username) (emailed-user :username)])
          _ (swirl-routes/handle-response (responded :id) nil "Loved it" recipient)
          _ (nagbot/email-user (emailed-user :id))
          ignored (create-swirl "generic" (author :id) "Thing that was ignored" "Yeah" [(recipient :username)])
          unrelated (create-swirl "generic" (author :id) "Thing that was never suggested" "Yeah" [])
          users-to-nag (nagbot/get-users-to-nag)
          awaiting (nagbot/get-unresponded-for-user (recipient :id))
          ]
      (is (= 1 (count (filter #(or (= (emailed-user :id) (:id %)) (= (recipient :id) (:id %))) users-to-nag))))
      (is (= 1 (count awaiting)))
      (nagbot/run-email-job)
      (let [swirl (first awaiting)]
        (is (= (swirl :id) (ignored :id)))))))

