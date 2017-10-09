(ns yswrl.rest.details-service
  (:require [clj-json.core :as json]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes context GET POST]]
            [yswrl.swirls.tmdb :as tmdb]
            [yswrl.swirls.itunes :as itunes]
            [yswrl.swirls.amazon :as amazon]
            [yswrl.swirls.boardgamegeek :as bgg]
            [yswrl.rest.utils :as rest-utils]))

(defn get-films-route []
  (GET "/film/:id" [id]
    (-> (tmdb/get-movie-from-tmdb-id id)
        rest-utils/json-response)))

(defn get-tv-route []
  (GET "/tv/:id" [id]
    (-> (tmdb/get-tv-from-tmdb-id id)
        rest-utils/json-response)))

(defn get-album-route []
  (GET "/album/:id" [id]
    (-> (itunes/get-itunes-album id)
        rest-utils/json-response)))

(defn get-book-route []
  (GET "/book/:id" [id]
    (-> (amazon/get-book id)
        rest-utils/json-response)))

(defn get-podcast-route []
  (GET "/podcast/:id" [id]
    (-> (itunes/get-itunes-podcast id)
        rest-utils/json-response)))

(defn get-app-route []
  (GET "/app/:id" [id]
    (-> (itunes/get-itunes-podcast id)
        rest-utils/json-response)))

(defn get-video-game-route []
  (GET "/videogame/:id" [id]
    (-> (amazon/get-game id)
        rest-utils/json-response)))

(defn get-board-game-route []
  (GET "/boardgame/:id" [id]
    (-> (bgg/get-by-id id)
        rest-utils/json-response)))

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

