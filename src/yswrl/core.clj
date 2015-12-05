(ns yswrl.core
  (:require [yswrl.handler :refer [app init]]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn parse-port [port]
  (Integer/parseInt (or port (System/getenv "PORT") "3000")))

(defn -main [& [port]]
  (let [port (parse-port port)]
    (init)
    (log/info "Starting server on port" port)
    (run-jetty app {:port port :join? false})))
