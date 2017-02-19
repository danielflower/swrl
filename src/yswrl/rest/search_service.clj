(ns yswrl.rest.search-service
  (:require [clj-json.core :as json]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes context GET POST]]
            [yswrl.swirls.tmdb :as tmdb]))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json"}
   :body    (try (json/generate-string data)
                 (catch Exception e
                   (log/error "Unable to parse JSON response. Exception: " e)
                   (json/generate-string {:error   (str "Unable to parse JSON response. Exception: " e)})))})

(defn search-films-route []
  (GET "/film" [query]
    (-> (tmdb/search-movies query)
        json-response)))

(defroutes swirl-search-routes
           (context "/api/v1/search" []
             (search-films-route)))

