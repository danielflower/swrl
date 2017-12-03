(ns yswrl.swirls.tmdb
  (:require [clj-http.client :as client]
            [clj-time.format :as f]
            [yswrl.links :as links]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

(def TMDB-API-KEY "c3356e66739e40233c7870d42b30bc34")
(def THUMBNAIL-URL-PREFIX "https://image.tmdb.org/t/p/original")
(def LARGE-IMAGE-URL-PREFIX "https://image.tmdb.org/t/p/original")

(defn release-year [rd]
  (if rd
    (if (> (.length rd) 4) (.substring rd 0 4))))


(defn search-movies
  ([search-term query-string]
   (try
     (if (clojure.string/blank? search-term)
       {:results []}
       (let [encoded (links/url-encode search-term)
             url (str "https://api.themoviedb.org/3/search/movie?api_key=" TMDB-API-KEY "&query=" encoded)
             result (client/get url {:accept :json :as :json})]
         {:results (map (fn [r] {:title           (str (r :title) " (" (release-year (r :release_date)) ")")
                                 :tmdb-id         (r :id)
                                 :overview        (r :overview)
                                 :create-url      (str "/create/movie?tmdb-id=" (r :id) "&" query-string)
                                 :large-image-url (str LARGE-IMAGE-URL-PREFIX (r :poster_path))
                                 :thumbnail-url   (str THUMBNAIL-URL-PREFIX (r :poster_path))}) ((result :body) :results))
          }))
     (catch Exception e
       (log/info e "Couldn't search movies with: " search-term)
       {:results []})))
  ([search-term]
   (search-movies search-term "")))

(defn get-the-best-overview [omdb-overview tmdb-overview]
  (if (and
        omdb-overview
        (not= "" omdb-overview)
        (not= "N/A" omdb-overview))
    omdb-overview
    tmdb-overview))

(defn get-movie-from-tmdb-id [tmdb-id]
  (let [url (str "https://api.themoviedb.org/3/movie/" tmdb-id "?api_key=" TMDB-API-KEY)
        result (try (client/get url {:accept :json :as :json})
                    (catch Exception e
                      (log/info e "Couldn't get IMDB ID from tmdb-id: " tmdb-id)
                      {}))
        body (result :body)
        omdb-body (:body (client/get (str "http://www.omdbapi.com/?apikey=d33a4ae1&i=" (:imdb_id body)) {:accept :json :as :json}))]
    {:title           (body :title)
     :overview        (get-the-best-overview (:Plot omdb-body) (body :overview))
     :release-year    (release-year (body :release_date))
     :thumbnail-url   (str THUMBNAIL-URL-PREFIX (body :poster_path))
     :large-image-url (str LARGE-IMAGE-URL-PREFIX (body :poster_path))
     :tmdb-id         (body :id)
     :imdb-id         (body :imdb_id)
     :url             (body :homepage)
     :tagline         (let [tagline (body :tagline)] (if (clojure.string/blank? tagline) "None" tagline))
     :genres          (map (fn [r] (r :name)) (body :genres))
     :director        (:Director omdb-body)
     :ratings         (:Ratings omdb-body)
     :runtime         (:Runtime omdb-body)
     :actors          (:Actors omdb-body)}
    ))

(defn get-tmdb-id-from-imdb-id [imdb-id]
  (let [url (str "https://api.themoviedb.org/3/find/" imdb-id "?api_key=" TMDB-API-KEY "&external_source=imdb_id")
        result (try (client/get url {:accept :json :as :json})
                    (catch Exception e
                      (log/info e "Couldn't get tmdb ID from imdb-id: " imdb-id)
                      {}))
        body (:body result)
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
  ([search-term query-string]
   (try
     (if (clojure.string/blank? search-term)
       {:results []}
       (let [encoded (links/url-encode search-term)
             url (str "https://api.themoviedb.org/3/search/tv?api_key=" TMDB-API-KEY "&query=" encoded)
             result (client/get url {:accept :json :as :json})]
         {:results (map (fn [r] {:title           (r :name)
                                 :tmdb-id         (r :id)
                                 :create-url      (str "/create/tv?tmdb-id=" (r :id) "&" query-string)
                                 :large-image-url (str LARGE-IMAGE-URL-PREFIX (r :poster_path))
                                 :thumbnail-url   (str THUMBNAIL-URL-PREFIX (r :poster_path))}) ((result :body) :results))
          }))
     (catch Exception e
       (log/info e "Couldn't search TV by: " search-term)
       {:results []})))
  ([search-term]
   (search-tv search-term ""))
  )

(defn get-tv-from-tmdb-id [tmdb-id]
  (let [url (str "https://api.themoviedb.org/3/tv/" tmdb-id "?api_key=" TMDB-API-KEY)
        result (try (client/get url {:accept :json :as :json})
                    (catch Exception e
                      (log/info e "Couldn't get TV by ID: " tmdb-id)
                      {}))
        body (result :body)]
    {:title           (body :name)
     :thumbnail-url   (str THUMBNAIL-URL-PREFIX (body :poster_path))
     :large-image-url (str LARGE-IMAGE-URL-PREFIX (body :poster_path))
     :tmdb-id         (body :id)
     :overview        (body :overview)
     :creator         (string/join ", " (map :name (body :created_by)))
     ;;:imdb-id (body :imdb_id) ;; API doesn't provide this yet, sadface
     :url             (body :homepage)
     :genres          (map :name (body :genres))
     :runtime         (reduce max (body :episode_run_time))
     }
    ))