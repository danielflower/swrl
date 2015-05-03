(ns yswrl.swirls.comment-notifier
  (:require [yswrl.db :as db]
            [yswrl.swirls.postman :as postman]
            [cronj.core :refer [cronj]]
            [taoensso.timbre :as timbre]
            [yswrl.swirls.swirls-repo :as repo]))
(use 'korma.core)

(defn email-addresses-of-swirl-author-and-commentors-excluding-current-comment-author [comment]
  (db/query "SELECT DISTINCT email FROM users WHERE id IN (
  SELECT author_id FROM swirls WHERE swirls.id = ?
  UNION
  SELECT author_id FROM comments WHERE swirl_id = ?
)
AND id != ?" (:swirl_id comment) (:swirl_id comment) (:author_id comment)))

(defn coment-notification-email-html [swirl comment]
  (let [values {:swirl swirl :comment comment :swirl_url (str "http://www.youshouldwatchreadlisten.com/swirls/" (:swirl_id comment))}]
    (postman/email-body "swirls/comment-notification-email.html" values)))

(defn send-comment-notification-emails [comment]
  (try
    (let [emails (email-addresses-of-swirl-author-and-commentors-excluding-current-comment-author comment)]
      (if (empty? emails)
        (timbre/info "There is no one to email for" comment)
        (do
          (timbre/info "Going to email" comment "to" emails)
          (let [swirl (repo/get-swirl (:swirl_id comment))
                subject (str "New comment on " (:title swirl))
                body (coment-notification-email-html swirl comment)
                to (map (fn [row] {:email (:email row) :name (:email row)}) emails)]
            (postman/send-email to subject body)))))
    (catch Exception e (timbre/error "Error sending comments email" e))))