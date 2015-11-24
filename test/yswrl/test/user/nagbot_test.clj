(ns yswrl.test.user.nagbot-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.user.nagbot :as nagbot]
            [yswrl.db :as db]
            [yswrl.user.notifications :refer [recommendation new-response new-comment mark-as-seen mark-email-sent users-with-pending-notifications create-notification-email-body]]
            [yswrl.user.notifications-repo :refer [get-for-user-email get-for-user-page]]
            [yswrl.swirls.swirl-routes :as swirl-routes]
            [korma.core
             :as k
             :refer [insert values where join fields set-fields select raw modifier]])
  (:use clojure.test))


(deftest nagging
  (let [author (create-test-user)]
    (testing "suggested but unanswered swirls are sent to the user if they have been registered long enough"
      (let [recipient (create-test-user)
            _ (k/update db/users
                      (set-fields {:date_registered (raw "(date_registered - inbox_email_interval) - INTERVAL '1 second'")}) ; pretend the user has been registered at least as long as the nagbot interval
                      (where {:id (recipient :id)}))
            emailed-user (create-test-user)
            responded (create-swirl "generic" (author :id) "Thing that was loved" "Yeah" [(recipient :username) (emailed-user :username)])
            _ (swirl-routes/handle-response (responded :id) nil "Loved it" recipient)
            _ (nagbot/email-user (emailed-user :id))
            ignored (create-swirl "generic" (author :id) "Thing that was ignored" "Yeah" [(recipient :username)])
            users-to-nag (nagbot/get-users-to-nag)
            awaiting (nagbot/get-unresponded-for-user (recipient :id))]
        (is (= 1 (count (filter #(or (= (emailed-user :id) (:id %)) (= (recipient :id) (:id %))) users-to-nag))))
        (is (= 1 (count awaiting)))
        (nagbot/run-email-job)
        (let [swirl (first awaiting)]
          (is (= (swirl :id) (ignored :id))))))

    (testing "suggested but unanswered swirls are not sent to new users"
      (let [recipient (create-test-user)
            _ignored (create-swirl "generic" (author :id) "Thing that was ignored" "Yeah" [(recipient :username)])
            users-to-nag (nagbot/get-users-to-nag)
            awaiting (nagbot/get-unresponded-for-user (recipient :id))]
        (is (= 0 (count (filter #(= (recipient :id) (:id %)) users-to-nag))))
        (is (= 1 (count awaiting)))))))