(ns yswrl.jobs
  (:require [yswrl.swirls.suggestion-job :as suggs]
            [clojure.tools.logging :as log]
            [yswrl.user.notifications :as notifications])
  (:use yswrl.layout)
  (:gen-class))


(defn -main []
  (log/info "Running scheduled jobs")
  (try
    (notifications/send-pending-notifications)
    (catch Exception e
      (log/error "Error while sending notifications" e)))
  (suggs/send-unsent-suggestions)
  (log/info "Scheduled jobs complete"))
