(ns yswrl.swirls.response-notifier
  (:require [yswrl.db :as db]
            [yswrl.swirls.postman :as postman]
            [cronj.core :refer [cronj]]
            [clojure.tools.logging :as log]
            [yswrl.swirls.swirls-repo :as repo]))
(use 'korma.core)

(defn email-addresses-of-swirl-author-and-responders-excluding-current-responder [response]
  (db/query "SELECT DISTINCT email FROM users WHERE id IN (
  SELECT author_id FROM swirls WHERE swirls.id = ?
  UNION
  SELECT responder FROM swirl_responses WHERE swirl_id = ?
)
AND id != ?" (:swirl_id response) (:swirl_id response) (:responder response)))

(defn response-notification-email-html [swirl response responder]
  (let [values {:swirl swirl :response response :responder responder}]
    (postman/email-body "swirls/response-notification-email.html" values)))

(defn send-response-notification-emails [response responder]
  (try
    (let [emails (email-addresses-of-swirl-author-and-responders-excluding-current-responder response)]
      (if (empty? emails)
        (log/info "There is no one to email for" response)
        (do
          (log/info "Going to email" response "to" emails)
          (let [swirl (repo/get-swirl (:swirl_id response))
                subject (str "New response to " (:title swirl))
                body (response-notification-email-html swirl response responder)
                to (map (fn [row] {:email (:email row) :name (:email row)}) emails)]
            (postman/send-email to subject body)))))
    (catch Exception e (log/error "Error sending response email" e))))