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

  (log/info "Updating game external ids")
  (repo/update-game-external-ids)
  (log/info "Updating game external ids complete")

  (log/info "Updating website external ids")
  (repo/update-website-external-ids)
  (log/info "Updating website external ids complete")


  (log/info "Scheduled jobs complete")
  )
