(ns yswrl.rest.search-service
  (:require [clj-json.core :as json]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes context GET POST]]
            [yswrl.swirls.tmdb :as tmdb]
            [yswrl.swirls.itunes :as itunes]
            [yswrl.swirls.amazon :as amazon]))

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

(defn search-tv-route []
  (GET "/tv" [query]
    (-> (tmdb/search-tv query)
        json-response)))

(defn search-album-route []
  (GET "/album" [query]
    (-> (itunes/search-albums query)
        json-response)))

(defn search-book-route []
  (GET "/book" [query]
    (-> (amazon/search-books query)
        json-response)))

(defn search-podcast-route []
  (GET "/podcast" [query]
    (-> (itunes/search-podcasts query)
        json-response)))

(defn search-app-route []
  (GET "/app" [query]
    (-> (itunes/search-apps query)
        json-response)))

(defn search-video-game-route []
  (GET "/videogame" [query]
    (-> (amazon/search-games query)
        json-response)))

(defroutes swirl-search-routes
           (context "/api/v1/search" []
             (search-films-route)
             (search-tv-route)
             (search-album-route)
             (search-book-route)
             (search-podcast-route)
             (search-app-route)
             (search-video-game-route)
             ))

