(ns yswrl.rest.utils
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json"}
   :body    (try (json/generate-string data)
                 (catch Exception e
                   (log/error "Unable to parse JSON response. Exception: " e)
                   (json/generate-string {:error (str "Unable to parse JSON response. Exception: " e)})))})