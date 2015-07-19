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
            [yswrl.swirls.swirl-links :as link-types]
            [yswrl.swirls.lookups :as lookups])
  (:import (java.net URI)))

(defn start-page [origin-swirl-id]
  (layout/render "swirls/start.html" {:origin-swirl-id origin-swirl-id}))

(defn session-from [req] (:user (:session req)))

(defn imdb-url [imdb-id]
  (str "http://www.imdb.com/title/" imdb-id))

(defn handle-website-creation [url author title origin-swirl-id]
  (let [metadata (website/get-metadata url)
        swirl-title (or title (metadata :title) "This website")
        embed-html (if (clojure.string/blank? (metadata :embed-html))
                     ""
                     (str (metadata :embed-html) "<p data-h=\"....or write something here\"></p>"))
        review (str "<p data-ph=\"Say something about this " (metadata :type) " here....\"></p>" embed-html)
        swirl (repo/save-draft-swirl (metadata :type) (author :id) swirl-title review (metadata :image-url))]
    (repo/add-link (swirl :id) (link-types/website-url :code) (str url))
    (redirect (links/edit-swirl (swirl :id) origin-swirl-id))))

(defn- host-ends-with [host test]
  (or (= host test) (.endsWith host (str "." test))))


(defn handle-album-creation [itunes-collection-id user origin-swirl-id]
  (let [album (itunes/get-itunes-album itunes-collection-id)
        title (album :title)
        thumbnail-url (album :thumbnail-url)
        track-html (clojure.string/join (map #(str "<li>" (% :track-name) "</li>") (album :tracks)))
        review (str "<p data-ph=\"Say something about this album here\"></p>"
                    "<p>Track listing:</p><ol>" track-html "</ol>")]
    (let [swirl (repo/save-draft-swirl "album" (user :id) title review thumbnail-url)]
      (repo/add-link (swirl :id) (link-types/itunes-id :code) itunes-collection-id)
      (redirect (links/edit-swirl (swirl :id) origin-swirl-id)))))

(defn handle-book-creation [asin user origin-swirl-id]
  (let [book (amazon/get-book asin)
        publish-line (if (clojure.string/blank? (book :author)) "" (str " by " (book :author)))
        title (str (book :title) publish-line)
        big-img-url (book :big-img-url)
        url (book :url)
        review "<p data-ph=\"Say something about this item here....\"></p>"
        swirl (repo/save-draft-swirl "book" (user :id) title review big-img-url)]
    (repo/add-link (swirl :id) (link-types/amazon-asin :code) asin)
    (repo/add-link (swirl :id) (link-types/amazon-url :code) url)
    (redirect (links/edit-swirl (swirl :id) origin-swirl-id))))

(defn handle-game-creation [asin user origin-swirl-id]
  (let [game (amazon/get-game asin)
        platform-line (if (clojure.string/blank? (game :platform)) "" (str " on " (game :platform)))
        title (str (game :title) platform-line)
        big-img-url (game :big-img-url)
        url (game :url)
        review "<p data-ph=\"Say something about this game here....\"></p>"
        swirl (repo/save-draft-swirl "game" (user :id) title review big-img-url)]
    (repo/add-link (swirl :id) (link-types/amazon-asin :code) asin)
    (repo/add-link (swirl :id) (link-types/amazon-url :code) url)
    (redirect (links/edit-swirl (swirl :id) origin-swirl-id))))

(defn handle-movie-creation
  ([tmdb-id user url origin-swirl-id]
   (if tmdb-id
     (let [movie (tmdb/get-movie-from-tmdb-id tmdb-id)
           review "<p data-ph=\"Say something about this movie here....\"></p>"
           swirl (repo/save-draft-swirl "movie" (user :id) (movie :title) review (movie :large-image-url))]
       (repo/add-link (swirl :id) (link-types/imdb-id :code) (movie :imdb-id))
       (redirect (links/edit-swirl (swirl :id) origin-swirl-id))
       )
     (handle-website-creation url user nil origin-swirl-id))
    )
  ([tmdb-id user origin-swirl-id]
   (handle-movie-creation tmdb-id user nil origin-swirl-id)))

(defn handle-tv-creation
  ([tmdb-id user url origin-swirl-id]
   (if tmdb-id
     (let [tv-show (tmdb/get-tv-from-tmdb-id tmdb-id)
           review "<p data-ph=\"Say something about this TV Show here....\"></p>"
           swirl (repo/save-draft-swirl "tv" (user :id) (tv-show :title) review (tv-show :large-image-url))]
       (repo/add-link (swirl :id) (link-types/website-url :code) (tv-show :url))
       (redirect (links/edit-swirl (swirl :id) origin-swirl-id))
       )
     (handle-website-creation url user nil origin-swirl-id))
    )
  ([tmdb-id user origin-swirl-id]
   (handle-tv-creation tmdb-id user nil origin-swirl-id)))

(defn handle-reswirl-creation [swirl-id author]
  (let [swirl (lookups/get-swirl swirl-id)
        review  "<p data-ph=\"Say something about this here....\"></p>"
        new-swirl (repo/save-draft-swirl (:type swirl) (author :id) (:title swirl) review (:thumbnail_url swirl))]
    (if-let [link (first(repo/get-links swirl-id))]
      (repo/add-link (new-swirl :id) (:type_code link) (:code link)))
    (redirect (links/edit-swirl (new-swirl :id) nil))))

(defn itunes-id-from-url [url]
  (let [[_ result] (re-find #"/id([\d]+)" url)]
    result))

(defn tmdb-id-from-url [url]
  (let [[_ result] (re-find #"/movie/([\d]+)\-.*" url)]
    result))

(defn imdb-id-from-url [url]
  (let [[_ result] (re-find #"/title/([^\#\?\/]+)" url)]
    result))

(defn search-music-page [search-term origin-swirl-id]
  (let [search-result (itunes/search-albums search-term origin-swirl-id)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "Album or Song" :origin-swirl-id origin-swirl-id})))

(defn search-books-page [search-term origin-swirl-id]
  (let [search-result (amazon/search-books search-term origin-swirl-id)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "Book title or author" :origin-swirl-id origin-swirl-id})))

(defn search-games-page [search-term origin-swirl-id]
  (let [search-result (amazon/search-games search-term origin-swirl-id)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "Game title" :origin-swirl-id origin-swirl-id})))

(defn search-movies-page [search-term origin-swirl-id]
  (let [search-result (tmdb/search-movies search-term origin-swirl-id)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "Movie name" :origin-swirl-id origin-swirl-id})))

(defn search-tv-page [search-term origin-swirl-id]
  (let [search-result (tmdb/search-tv search-term origin-swirl-id)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "TV show title" :origin-swirl-id origin-swirl-id})))


(defn asin-from-url [url]
  (let [[_ result] (re-find #"/([0-9A-Z]{10})(?:[/?]|$)" url)]
    result))

(defn handle-itunes-creation [url user _ origin-swirl-id]
  (handle-album-creation (itunes-id-from-url (str url)) user origin-swirl-id))

(defn handle-amazon-creation [url user _ origin-swirl-id]
  (handle-book-creation (asin-from-url (str url)) user origin-swirl-id))

(defn handle-tmdb-creation [url user _ origin-swirl-id]
  (handle-movie-creation (tmdb-id-from-url (str url)) user origin-swirl-id))

(defn handle-imdb-creation [url user _ origin-swirl-id]
  (if-let [{tmdb-id :tmdb-id type :type} (tmdb/get-tmdb-id-from-imdb-id (imdb-id-from-url (str url)))]
    (case type
      "movie" (handle-movie-creation tmdb-id user url origin-swirl-id)
      "tv" (handle-tv-creation tmdb-id user url origin-swirl-id)
      (handle-website-creation url user nil origin-swirl-id))
    (handle-website-creation url user nil origin-swirl-id)))

(defn handler-for [url]
  (let [host (.getHost url)]
    (cond (host-ends-with host "amazon.com") handle-amazon-creation
          (host-ends-with host "itunes.apple.com") handle-itunes-creation
          (host-ends-with host "themoviedb.org") handle-tmdb-creation
          (host-ends-with host "imdb.com") handle-imdb-creation
          :else handle-website-creation)
    ))

(defn handle-creation-from-url [url title author origin-swirl-id]
  (try
    ((handler-for url) url author title origin-swirl-id)
    (catch Exception e
      (log/warn (str "Error while handling " url author title origin-swirl-id " so will fall back to generic handler. Error was") e)
      (handle-website-creation url author title origin-swirl-id)
      )))


(defn create-from-url-handler [url title origin-swirl-id req]
  (let [uri (URI. url)]
    (if (= "chrome" (.getScheme (URI. url)))
      (redirect "/")
      (guard/requires-login #(handle-creation-from-url uri title (session-from req) origin-swirl-id)))))


(defroutes creation-routes
           (GET "/swirls/start" [origin-swirl-id] (start-page origin-swirl-id))
           (GET "/search/music" [search-term origin-swirl-id] (search-music-page search-term origin-swirl-id))
           (GET "/search/books" [search-term origin-swirl-id] (search-books-page search-term origin-swirl-id))
           (GET "/search/movies" [search-term origin-swirl-id] (search-movies-page search-term origin-swirl-id))
           (GET "/search/games" [search-term origin-swirl-id] (search-games-page search-term origin-swirl-id))
           (GET "/search/tv" [search-term origin-swirl-id] (search-tv-page search-term origin-swirl-id))

           (GET "/create/from-url" [url title origin-swirl-id :as req] (create-from-url-handler url title origin-swirl-id req))
           (GET "/create/album" [itunes-album-id origin-swirl-id :as req] (guard/requires-login #(handle-album-creation itunes-album-id (session-from req) origin-swirl-id)))
           (GET "/create/book" [book-id origin-swirl-id :as req] (guard/requires-login #(handle-book-creation book-id (session-from req) origin-swirl-id)))
           (GET "/create/game" [game-id origin-swirl-id :as req] (guard/requires-login #(handle-game-creation game-id (session-from req) origin-swirl-id)))
           (GET "/create/movie" [tmdb-id origin-swirl-id :as req] (guard/requires-login #(handle-movie-creation tmdb-id (session-from req) origin-swirl-id)))
           (GET "/create/tv" [tmdb-id origin-swirl-id :as req] (guard/requires-login #(handle-tv-creation tmdb-id (session-from req) origin-swirl-id)))
           (GET "/create/reswirl" [id :as req] (guard/requires-login #(handle-reswirl-creation (Long/parseLong id) (session-from req))))
           )