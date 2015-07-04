(ns yswrl.user.nagbot
  (:require [yswrl.swirls.lookups :as lookups]
            [yswrl.db :as db]
            [yswrl.swirls.postman :as postman])
  (:import (java.sql Timestamp)))
(use 'korma.core)
(use 'korma.db)

(defn now [] (Timestamp. (System/currentTimeMillis)))


(defn get-unresponded-for-user [user-id]
  (lookups/get-swirls-awaiting-response user-id 100 0))

(defn get-users-to-nag []
  (db/query "SELECT id FROM users WHERE
  id IN (SELECT recipient_id FROM suggestions WHERE recipient_id IS NOT NULL AND response_id IS NULL)
  AND (date_last_nagged IS NULL OR date_last_nagged < (now() - interval '1 week'))
  "))

(defn email-user [user-id]
  (let [swirls (get-unresponded-for-user user-id)
        recipient (yswrl.auth.auth-repo/get-user-by-id user-id)]
    (postman/send-email [{:email (:email recipient) :name (:username recipient)}]
                        "Swirls awaiting your response"
                        (postman/email-body "notifications/nag-email.html"
                                            {:swirls swirls :recipient recipient}))
    (update db/users
            (set-fields {:date_last_nagged (now)})
            (where {:id user-id}))))

(defn run-email-job []
  (let [users (get-users-to-nag)
        ids (map :id users)]
    (doseq [id ids] (email-user id))))
