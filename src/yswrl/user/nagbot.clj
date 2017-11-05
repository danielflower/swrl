(ns yswrl.user.nagbot
  (:require [yswrl.swirls.lookups :as lookups]
            [yswrl.db :as db]
            [korma.core
             :as k
             :refer [insert values where join fields set-fields select raw modifier]]
            [yswrl.swirls.mailgun :as mailgun])
  (:import (java.sql Timestamp)))
(use 'korma.db)

(defn now [] (Timestamp. (System/currentTimeMillis)))


(defn get-unresponded-for-user [user-id]
  (lookups/get-swirls-awaiting-response {:id user-id} 100 0))

(defn get-users-to-nag []
  (db/query "SELECT id FROM users WHERE
  id IN (SELECT recipient_id FROM suggestions WHERE recipient_id IS NOT NULL AND response_id IS NULL)
  AND COALESCE(date_last_nagged, date_registered) < (now() - inbox_email_interval)"))

(defn email-user [user-id]
  (let [swirls (get-unresponded-for-user user-id)
        recipient (yswrl.auth.auth-repo/get-user-by-id user-id)]
    (mailgun/send-email (:email recipient)
                        "Swirls awaiting your response"
                        (mailgun/email-body "notifications/nag-email.html"
                                            {:swirls swirls :recipient recipient}))
    (k/update db/users
            (set-fields {:date_last_nagged (now)})
            (where {:id user-id}))))

(defn run-email-job []
  (let [users (get-users-to-nag)
        ids (map :id users)]
    (doseq [id ids] (email-user id))))
