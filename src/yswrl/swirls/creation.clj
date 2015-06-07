(ns yswrl.swirls.creation
  (:require [yswrl.links :as links]
            [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect response not-found]]
            [yswrl.swirls.itunes :as itunes]
            [yswrl.swirls.amazon :as amazon]
            [ring.util.response :refer [redirect response not-found]]
            [yswrl.auth.guard :as guard]
            [yswrl.swirls.tmdb :as tmdb]
            [yswrl.swirls.website :as website]
            [yswrl.swirls.tmdb :as tmdb]
            [clojure.tools.logging :as log]
            [yswrl.swirls.swirl-links :as link-types])
  (:import (java.net URI)))

(defn start-page []
  (layout/render "swirls/start.html"))

(defn session-from [req] (:user (:session req)))

(defn imdb-url [imdb-id]
  (str "http://www.imdb.com/title/" imdb-id))

(defn handle-website-creation [url author title]
  (let [metadata (website/get-metadata url)
        _ (log/debug "Metadata for" url ":" metadata)
        swirl-title (or title (metadata :title) "This website")
        image-tag (if (not (nil? (metadata :image-url))) (str "<img width=\"200\" src=\"" (metadata :image-url) "\"><p>") "")
        review (str (or (metadata :embed-html) image-tag)
                    "<p>Check out: <a href=\"" url "\">" url "</a></p>"
                    (metadata :description))
        swirl (repo/save-draft-swirl (metadata :type) (author :id) swirl-title review (metadata :image-url))]
    (repo/add-link (swirl :id) (link-types/website-url :code) (str url))
    (redirect (links/edit-swirl (swirl :id)))))

(defn- host-ends-with [host test]
  (or (= host test) (.endsWith host (str "." test))))


(defn handle-album-creation [itunes-collection-id user]
  (let [album (itunes/get-itunes-album itunes-collection-id)
        title (album :title)
        thumbnail-url (album :thumbnail-url)
        track-html (clojure.string/join (map #(str "<li>" (% :track-name) "</li>") (album :tracks)))
        review (str "<img src=\"" thumbnail-url "\"><p>Track listing:</p><ol>" track-html "</ol><p>What do you think?</p>")]
    (let [swirl (repo/save-draft-swirl "album" (user :id) title review thumbnail-url)]
      (repo/add-link (swirl :id) (link-types/itunes-id :code) itunes-collection-id)
      (redirect (links/edit-swirl (swirl :id))))))

(defn handle-book-creation [asin user]
  (let [book (amazon/get-book asin)
        publish-line (if (clojure.string/blank? (book :author)) "" (str " by " (book :author)))
        title (str (book :title) publish-line)
        big-img-url (book :big-img-url)
        book-html (book :blurb)
        url (book :url)
        review (str "<img src=\"" big-img-url "\"><p>Blurb:</p>" book-html "<p>What do you think?</p>")
        swirl (repo/save-draft-swirl "book" (user :id) title review big-img-url)]
    (repo/add-link (swirl :id) (link-types/amazon-asin :code) asin)
    (repo/add-link (swirl :id) (link-types/amazon-url :code) url)
    (redirect (links/edit-swirl (swirl :id)))))

(defn handle-movie-creation
  ([tmdb-id user url]
   (if tmdb-id
     (let [movie (tmdb/get-movie-from-tmdb-id tmdb-id)
           review (str "<img src=\"" (movie :large-image-url) "\"><p>"
                       (if (not (clojure.string/blank? (movie :url))) (str "<a href=\"" (movie :url) "\">Official Movie Homepage</a></br>") "")
                       "<p>Tagline: " (movie :tagline) "</p>"
                       "<p>Movie Overview:</p>" (movie :overview)
                       "</br></br><p>What do you think?</p>")
           swirl (repo/save-draft-swirl "movie" (user :id) (movie :title) review (movie :large-image-url))]
       (repo/add-link (swirl :id) (link-types/imdb-id :code) (movie :imdb-id))
       (redirect (links/edit-swirl (swirl :id)))
       )
     (handle-website-creation url user nil))
    )
  ([tmdb-id user]
   (handle-movie-creation tmdb-id user nil)))

(defn itunes-id-from-url [url]
  (let [[_ result] (re-find #"/id([\d]+)" url)]
    result))

(defn tmdb-id-from-url [url]
  (let [[_ result] (re-find #"/movie/([\d]+)\-.*" url)]
    result))

(defn imdb-id-from-url [url]
  (let [[_ result] (re-find #"/title/([^\#\?\/]+)" url)]
    result))

(defn search-music-page [search-term]
  (let [search-result (itunes/search-albums search-term)]
    (layout/render "swirls/search.html" {:search-term search-term :search-result search-result})))

(defn search-books-page [search-term]
  (let [search-result (amazon/search-books search-term)]
    (layout/render "swirls/search_books.html" {:search-term search-term :search-result search-result})))

(defn search-movies-page [search-term]
  (let [search-result (tmdb/search-movies search-term)]
    (layout/render "swirls/search_movies.html" {:search-term search-term :search-result search-result})))

(defn asin-from-url [url]
  (let [[_ result] (re-find #"/([0-9A-Z]{10})(?:[/?]|$)" url)]
    result))

(defn handle-itunes-creation [url user _]
  (handle-album-creation (itunes-id-from-url (str url)) user))

(defn handle-amazon-creation [url user _]
  (handle-book-creation (asin-from-url (str url)) user))

(defn handle-tmdb-creation [url user _]
  (handle-movie-creation (tmdb-id-from-url (str url)) user))

(defn handle-imdb-creation [url user _]
  (handle-movie-creation (tmdb/get-tmdb-id-from-imdb-id (imdb-id-from-url (str url))) user url))

(defn handler-for [url]
  (let [host (.getHost url)]
    (cond (host-ends-with host "amazon.com") handle-amazon-creation
          (host-ends-with host "itunes.apple.com") handle-itunes-creation
          (host-ends-with host "themoviedb.org") handle-tmdb-creation
          (host-ends-with host "imdb.com") handle-imdb-creation
          :else handle-website-creation)
    ))

(defn handle-creation-from-url [url title author]
  ((handler-for url) url author title))


(defn create-from-url-handler [url title req]
  (let [uri (URI. url)]
    (if (= "chrome" (.getScheme (java.net.URI. url)))
      (redirect "/")
      (guard/requires-login #(handle-creation-from-url uri title (session-from req))))))

(defroutes creation-routes
           (GET "/swirls/start" [] (start-page))
           (GET "/search/music" [search-term] (search-music-page search-term))
           (GET "/search/books" [search-term] (search-books-page search-term))
           (GET "/search/movies" [search-term] (search-movies-page search-term))

           (GET "/create/from-url" [url title :as req] (create-from-url-handler url title req))
           (GET "/create/album" [itunes-album-id :as req] (guard/requires-login #(handle-album-creation itunes-album-id (session-from req))))
           (GET "/create/book" [book-id :as req] (guard/requires-login #(handle-book-creation book-id (session-from req))))
           (GET "/create/movie" [tmdb-id :as req] (guard/requires-login #(handle-movie-creation tmdb-id (session-from req))))
           )