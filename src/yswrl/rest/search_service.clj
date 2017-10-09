(ns yswrl.rest.search-service
  (:require [clj-json.core :as json]
            [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes context GET POST]]
            [yswrl.swirls.tmdb :as tmdb]
            [yswrl.swirls.itunes :as itunes]
            [yswrl.swirls.amazon :as amazon]
            [yswrl.swirls.boardgamegeek :as bgg]
            [yswrl.rest.utils :as rest-utils]))

(defn search-films-route []
  (GET "/film" [query]
    (-> (tmdb/search-movies query)
        rest-utils/json-response)))

(defn search-tv-route []
  (GET "/tv" [query]
    (-> (tmdb/search-tv query)
        rest-utils/json-response)))

(defn search-album-route []
  (GET "/album" [query]
    (-> (itunes/search-albums query)
        rest-utils/json-response)))

(defn search-book-route []
  (GET "/book" [query]
    (-> (amazon/search-books query)
        rest-utils/json-response)))

(defn search-podcast-route []
  (GET "/podcast" [query]
    (-> (itunes/search-podcasts query)
        rest-utils/json-response)))

(defn search-app-route []
  (GET "/app" [query]
    (-> (itunes/search-apps query)
        rest-utils/json-response)))

(defn search-video-game-route []
  (GET "/videogame" [query]
    (-> (amazon/search-games query)
        rest-utils/json-response)))

(defn search-board-game-route []
  (GET "/boardgame" [query]
    (-> (bgg/search query)
        rest-utils/json-response)))

(defroutes swirl-search-routes
           (context "/api/v1/search" []
             (search-films-route)
             (search-tv-route)
             (search-album-route)
             (search-book-route)
             (search-podcast-route)
             (search-app-route)
             (search-video-game-route)
             (search-board-game-route)
             ))

