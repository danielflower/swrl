(ns yswrl.rest.details-service
  (:require [clj-json.core :as json]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes context GET POST]]
            [yswrl.swirls.tmdb :as tmdb]
            [yswrl.swirls.itunes :as itunes]
            [yswrl.swirls.amazon :as amazon]
            [yswrl.swirls.boardgamegeek :as bgg]
            ))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/json"}
   :body    (try (json/generate-string data)
                 (catch Exception e
                   (log/error "Unable to parse JSON response. Exception: " e)
                   (json/generate-string {:error   (str "Unable to parse JSON response. Exception: " e)})))})

(defn get-films-route []
  (GET "/film/:id" [id]
    (-> (tmdb/get-movie-from-tmdb-id id)
        json-response)))

(defn get-tv-route []
  (GET "/tv/:id" [id]
    (-> (tmdb/get-tv-from-tmdb-id id)
        json-response)))

(defn get-album-route []
  (GET "/album/:id" [id]
    (-> (itunes/get-itunes-album id)
        json-response)))

(defn get-book-route []
  (GET "/book/:id" [id]
    (-> (amazon/get-book id)
        json-response)))

(defn get-podcast-route []
  (GET "/podcast/:id" [id]
    (-> (itunes/get-itunes-podcast id)
        json-response)))

(defn get-app-route []
  (GET "/app/:id" [id]
    (-> (itunes/get-itunes-podcast id)
        json-response)))

(defn get-video-game-route []
  (GET "/videogame/:id" [id]
    (-> (amazon/get-game id)
        json-response)))

(defn get-board-game-route []
  (GET "/boardgame/:id" [id]
    (-> (bgg/get-by-id id)
        json-response)))

(defroutes swirl-details-routes
           (context "/api/v1/details" []
             (get-films-route)
             (get-tv-route)
             (get-album-route)
             (get-book-route)
             (get-podcast-route)
             (get-app-route)
             (get-video-game-route)
             (get-board-game-route)
             ))

