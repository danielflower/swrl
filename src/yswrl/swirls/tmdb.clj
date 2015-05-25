(ns yswrl.swirls.tmdb
  (:require [clj-http.client :as client]
            [yswrl.links :as links]))

(def TMDB-API-KEY "c3356e66739e40233c7870d42b30bc34")
(def THUMBNAIL-URL-PREFIX "http://image.tmdb.org/t/p/w92")
(def LARGE-IMAGE-URL-PREFIX "http://image.tmdb.org/t/p/w342")

(defn search-movies [search-term]
  (if (clojure.string/blank? search-term)
    { :results [] }
    (let [encoded (links/url-encode search-term)
          url (str "https://api.themoviedb.org/3/search/movie?api_key=" TMDB-API-KEY "&query=" encoded)
          result (client/get url {:accept :json :as :json})] {
                                                              :results (map (fn [r] {:title         (r :title)
                                                                                     :tmdb-id        (r :id)
                                                                                     :large-image-url    (str LARGE-IMAGE-URL-PREFIX (r :poster_path))
                                                                                     :thumbnail-url (str THUMBNAIL-URL-PREFIX (r :poster_path))}) ((result :body) :results))
                                                              })))

(defn get-movie-from-tmdb-id [tmdb-id]
  (let [url (str "https://api.themoviedb.org/3/movie/" tmdb-id "?api_key=" TMDB-API-KEY)
        result (client/get url {:accept :json :as :json})
        body (result :body)]
    {:title         (body :title)
     :overview   (body :overview)
     :thumbnail-url (str THUMBNAIL-URL-PREFIX (body :poster_path))
     :large-image-url (str LARGE-IMAGE-URL-PREFIX (body :poster_path))
     :tmdb-id (body :id)
     :imdb-id (body :imdb_id)
     :url (body :homepage)
     :tagline (let [tagline (body :tagline)] (if (clojure.string/blank? tagline) "None" tagline) )
     :genres        (map (fn [r] {:genre (r :name)}) (body :genres))}
    ))