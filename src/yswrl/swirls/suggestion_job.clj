(ns yswrl.swirls.suggestion-job
  (:require [yswrl.db :as db]
            [yswrl.swirls.postman :refer [send-email email-body]]
            [clojure.tools.logging :as log]
            [korma.core :as k])
  (:import (java.sql Timestamp)))

; Checks the suggestions table and emails any outstanding suggestions


(defn get-unsent []
  (db/query "SELECT
  suggestions.id as suggestion_id, suggestions.code, suggestions.swirl_id,
  swirls.title, author.username AS author_name, suggestions.recipient_email
FROM ((suggestions INNER JOIN swirls ON swirls.id = suggestions.swirl_id)
    INNER JOIN users AS author ON author.id = swirls.author_id)
WHERE (suggestions.recipient_id IS NULL AND suggestions.mandrill_id IS NULL AND suggestions.mandrill_rejection_reason IS NULL)"))


(defn now [] (Timestamp. (System/currentTimeMillis)))

(defn mark-suggestion-sent [suggestion-id mandrill_id]
  (k/update db/suggestions
          (k/set-fields {:mandrill_id mandrill_id :date_notified (now)})
          (k/where {:id [= suggestion-id]})))

(defn mark-suggestion-failed [suggestion-id rejection-reason]
  (k/update db/suggestions
          (k/set-fields {:mandrill_rejection_reason rejection-reason :date_notified (now)})
          (k/where {:id [= suggestion-id]})))

(defn suggestion-email-html [row]
  (email-body "swirls/suggestion-email.html" row))

(defn send-unsent-suggestions []
  (try
    (let [unsent (get-unsent)]
      (log/debug "Suggestion emailer processing" (count unsent) "suggestions")
      (doseq [row unsent]
        (log/debug "About to process" row)
        (let [[response]
              (send-email (:recipient_email row) (str "New recommendation from " (:author_name row)) (suggestion-email-html row))]
          (if (or (= (:status response) "sent") (= (:status response) "queued") (= (:status response) "scheduled"))
            (mark-suggestion-sent (:suggestion_id row) (:_id response))
            (mark-suggestion-failed (:suggestion_id row) (str (:status response) " " (:reject_reason response)))))))
    (catch Exception e (log/error "Error sending suggestions" e))))
