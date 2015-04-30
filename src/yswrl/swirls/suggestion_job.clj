(ns yswrl.swirls.suggestion-job
  (:require [yswrl.db :as db]
            [yswrl.swirls.postman :refer [send-email]]
            [cronj.core :refer [cronj]]
            [taoensso.timbre :as timbre]))
(use 'korma.core)
(use 'selmer.parser)

; Checks the suggestions table and emails any outstanding suggestions


(defn get-unsent []
  (exec-raw "SELECT
  suggestions.id as suggestion_id, suggestions.swirl_id,
  swirls.title, author.username AS author_name,
  COALESCE(recipient.email, suggestions.recipient_email) AS recipient_email
FROM ((suggestions INNER JOIN swirls ON swirls.id = suggestions.swirl_id)
    INNER JOIN users AS author ON author.id = swirls.author_id)
  LEFT JOIN users AS recipient ON recipient.id = suggestions.recipient_id
WHERE (suggestions.mandrill_id IS NULL AND suggestions.mandrill_rejection_reason IS NULL)" :results))


(defn now [] (java.sql.Timestamp. (System/currentTimeMillis)))

(defn mark-suggestion-sent [suggestion-id mandrill_id]
  (update db/suggestions
          (set-fields {:mandrill_id mandrill_id :date_notified (now)})
          (where {:id [= suggestion-id]})))

(defn mark-suggestion-failed [suggestion-id rejection-reason]
  (update db/suggestions
          (set-fields {:mandrill_rejection_reason rejection-reason :date_notified (now)})
          (where {:id [= suggestion-id]})))

(defn suggestion-email-html [row]
  (let [values (assoc row :swirl_url (str "http://www.youshouldwatchreadlisten.com/swirls/" (:swirl_id row)))]
    (render-file "/swirls/suggestion-email.html" values)))

(defn send-unsent-suggestions []
  (try
    (let [unsent (get-unsent)]
      (timbre/info "Suggestion emailer processing" (count unsent) "suggestions")
      (for [row unsent]
        (let [[response]
              (send-email (:recipient_email row) (:recipient_email row) (str "New recommendation from " (:author_name row)) (suggestion-email-html row))]
          (if (or (= (:status response) "sent") (= (:status response) "queued") (= (:status response) "scheduled"))
            (mark-suggestion-sent (:suggestion_id row) (:_id response))
            (mark-suggestion-failed (:suggestion_id row) (str (:status response) " " (:reject_reason response)))))))
    (catch Exception e (timbre/error "Error sending suggestions" e))))

(def send-unsent-suggestions-job
  (cronj
    :entries
    [{:id       "send-unsent-suggestions"
      :handler  (fn [_ _] (send-unsent-suggestions))
      :schedule "/60 * * * * * *"
      :opts     {}}]))