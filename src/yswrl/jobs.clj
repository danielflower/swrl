(ns yswrl.jobs
  (:require [yswrl.swirls.suggestion-job :as suggs]
            [clojure.tools.logging :as log]
            [yswrl.user.notifications :as notifications]
            [yswrl.user.nagbot :as nagbot]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.auth.auth-repo :as auth-repo])
  (:use yswrl.layout)
  (:gen-class))

(defn -main []
  (log/info "Running scheduled jobs")

  (try
    (notifications/send-pending-notifications)
    (catch Exception e
      (log/error "Error while sending notifications" e)))

  (try
    (nagbot/run-email-job)
    (catch Exception e
      (log/error "Error while sending reminders" e)))

  (suggs/send-unsent-suggestions)

  (log/info "Starting Friend Weighting Updates")
  (repo/update-weightings-for-friend-changes (mapv :user-id (auth-repo/get-all-users)))
  (log/info "Friend Weighting Updates complete")

  ; Probably shouldn't run this all the time as I think the APIs limit calls per day
  ; (repo/update-all-details)

  (log/info "Scheduled jobs complete")
  )
