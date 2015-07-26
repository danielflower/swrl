(ns yswrl.swirls.tmdb
  (:require [clj-http.client :as client]
            [yswrl.links :as links]))

(def TMDB-API-KEY "c3356e66739e40233c7870d42b30bc34")
(def THUMBNAIL-URL-PREFIX "http://image.tmdb.org/t/p/original")
(def LARGE-IMAGE-URL-PREFIX "http://image.tmdb.org/t/p/original")

(defn search-movies
  ([search-term origin-swirl-id]
   (if (clojure.string/blank? search-term)
     {:results []}
     (let [encoded (links/url-encode search-term)
           url (str "https://api.themoviedb.org/3/search/movie?api_key=" TMDB-API-KEY "&query=" encoded)
           result (client/get url {:accept :json :as :json})] {
                                                               :results (map (fn [r] {:title           (r :title)
                                                                                      :tmdb-id         (r :id)
                                                                                      :create-url      (str "/create/movie?tmdb-id=" (r :id)
                                                                                                            (if (clojure.string/blank? origin-swirl-id)
                                                                                                              nil
                                                                                                              (str "&origin-swirl-id=" origin-swirl-id)))
                                                                                      :large-image-url (str LARGE-IMAGE-URL-PREFIX (r :poster_path))
                                                                                      :thumbnail-url   (str THUMBNAIL-URL-PREFIX (r :poster_path))}) ((result :body) :results))
                                                               })))
  ([search-term]
   (search-movies search-term "")))

(defn get-movie-from-tmdb-id [tmdb-id]
  (let [url (str "https://api.themoviedb.org/3/movie/" tmdb-id "?api_key=" TMDB-API-KEY)
        result (client/get url {:accept :json :as :json})
        body (result :body)]
    {:title           (body :title)
     :overview        (body :overview)
     :thumbnail-url   (str THUMBNAIL-URL-PREFIX (body :poster_path))
     :large-image-url (str LARGE-IMAGE-URL-PREFIX (body :poster_path))
     :tmdb-id         (body :id)
     :imdb-id         (body :imdb_id)
     :url             (body :homepage)
     :tagline         (let [tagline (body :tagline)] (if (clojure.string/blank? tagline) "None" tagline))
     :genres          (map (fn [r] {:genre (r :name)}) (body :genres))}
    ))

(defn get-tmdb-id-from-imdb-id [imdb-id]
  (let [url (str "https://api.themoviedb.org/3/find/" imdb-id "?api_key=" TMDB-API-KEY "&external_source=imdb_id")
        result (client/get url {:accept :json :as :json})
        body (result :body)
        movie_id (:id (first (:movie_results body)))
        tv_id (:id (first (:tv_results body)))]
    (if (not (nil? movie_id))
      {:tmdb-id movie_id
       :type    "movie"}
      (if (not (nil? tv_id))
        {:tmdb-id tv_id
         :type    "tv"}
        nil))
    ))

(defn search-tv
  ([search-term origin-swirl-id]
   (if (clojure.string/blank? search-term)
     {:results []}
     (let [encoded (links/url-encode search-term)
           url (str "https://api.themoviedb.org/3/search/tv?api_key=" TMDB-API-KEY "&query=" encoded)
           result (client/get url {:accept :json :as :json})] {
                                                               :results (map (fn [r] {:title           (r :name)
                                                                                      :tmdb-id         (r :id)
                                                                                      :create-url      (str "/create/tv?tmdb-id=" (r :id)
                                                                                                            (if (clojure.string/blank? origin-swirl-id)
                                                                                                              nil
                                                                                                              (str "&origin-swirl-id=" origin-swirl-id)))
                                                                                      :large-image-url (str LARGE-IMAGE-URL-PREFIX (r :poster_path))
                                                                                      :thumbnail-url   (str THUMBNAIL-URL-PREFIX (r :poster_path))}) ((result :body) :results))
                                                               })))
  ([search-term]
   (search-tv search-term ""))
  )

(defn get-tv-from-tmdb-id [tmdb-id]
  (let [url (str "https://api.themoviedb.org/3/tv/" tmdb-id "?api_key=" TMDB-API-KEY)
        result (client/get url {:accept :json :as :json})
        body (result :body)]
    {:title           (body :name)
     :thumbnail-url   (str THUMBNAIL-URL-PREFIX (body :poster_path))
     :large-image-url (str LARGE-IMAGE-URL-PREFIX (body :poster_path))
     :tmdb-id         (body :id)
     ;;:imdb-id (body :imdb_id) ;; API doesn't provide this yet, sadface
     :url             (body :homepage)
     }
    ))