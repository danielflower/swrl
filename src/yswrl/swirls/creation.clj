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

(defn start-page [req]
  (layout/render "swirls/start.html" {:title "Create a recommendation" :query-string (:query-string req)}))

(defn session-from [req] (:user (:session req)))

(defn imdb-url [imdb-id]
  (str "http://www.imdb.com/title/" imdb-id))

(defn handle-website-creation [url author title query-string]
  (let [metadata (website/get-metadata url)
        swirl-title (or title (metadata :title) "This website")
        embed-html (if (clojure.string/blank? (metadata :embed-html))
                     ""
                     (str (metadata :embed-html) "<p data-h=\"....or write something here\"></p>"))
        review (str "<p data-ph=\"Why should your friends see this?\"></p>" embed-html)
        swirl (repo/save-draft-swirl (metadata :type) (author :id) swirl-title review (metadata :image-url))]
    (repo/add-link (swirl :id) (link-types/website-url :code) (str url))
    (redirect (links/edit-swirl (swirl :id) query-string))))

(defn- host-ends-with [host test]
  (or (= host test) (.endsWith host (str "." test))))


(defn handle-album-creation [itunes-collection-id user query-string]
  (let [album (itunes/get-itunes-album itunes-collection-id)
        title (album :title)
        thumbnail-url (album :thumbnail-url)
        track-html (clojure.string/join (map #(str "<li>" (% :track-name) "</li>") (album :tracks)))
        review (str "<p data-ph=\"Why should your friends listen to this album?\"></p>"
                    "<p>Track listing:</p><ol>" track-html "</ol>")]
    (let [swirl (repo/save-draft-swirl "album" (user :id) title review thumbnail-url)]
      (repo/add-link (swirl :id) (link-types/itunes-id :code) itunes-collection-id)
      (redirect (links/edit-swirl (swirl :id) query-string)))))

(defn handle-book-creation [asin user query-string]
  (let [book (amazon/get-book asin)
        publish-line (if (clojure.string/blank? (book :author)) "" (str " by " (book :author)))
        title (str (book :title) publish-line)
        big-img-url (book :big-img-url)
        url (book :url)
        review "<p data-ph=\"Why should your friends read this book?\"></p>"
        swirl (repo/save-draft-swirl "book" (user :id) title review big-img-url)]
    (repo/add-link (swirl :id) (link-types/amazon-asin :code) asin)
    (repo/add-link (swirl :id) (link-types/amazon-url :code) url)
    (redirect (links/edit-swirl (swirl :id) query-string))))

(defn handle-game-creation [asin user query-string]
  (let [game (amazon/get-game asin)
        platform-line (if (clojure.string/blank? (game :platform)) "" (str " on " (game :platform)))
        title (str (game :title) platform-line)
        big-img-url (game :big-img-url)
        url (game :url)
        review "<p data-ph=\"Why should your friends play this game?\"></p>"
        swirl (repo/save-draft-swirl "game" (user :id) title review big-img-url)]
    (repo/add-link (swirl :id) (link-types/amazon-asin :code) asin)
    (repo/add-link (swirl :id) (link-types/amazon-url :code) url)
    (redirect (links/edit-swirl (swirl :id) query-string))))

(defn handle-movie-creation
  ([tmdb-id user url query-string]
   (if tmdb-id
     (let [movie (tmdb/get-movie-from-tmdb-id tmdb-id)
           review "<p data-ph=\"Why should your friends watch this movie?\"></p>"
           title (str (movie :title) " (" (movie :release-year) ")")
           swirl (repo/save-draft-swirl "movie" (user :id) title review (movie :large-image-url))]
       (repo/add-link (swirl :id) (link-types/imdb-id :code) (movie :imdb-id))
       (redirect (links/edit-swirl (swirl :id) query-string))
       )
     (handle-website-creation url user nil query-string))
    )
  ([tmdb-id user query-string]
   (handle-movie-creation tmdb-id user nil query-string)))

(defn handle-tv-creation
  ([tmdb-id user url query-string]
   (if tmdb-id
     (let [tv-show (tmdb/get-tv-from-tmdb-id tmdb-id)
           review "<p data-ph=\"Why should your friends watch this TV show?\"></p>"
           swirl (repo/save-draft-swirl "tv" (user :id) (tv-show :title) review (tv-show :large-image-url))]
       (repo/add-link (swirl :id) (link-types/website-url :code) (tv-show :url))
       (redirect (links/edit-swirl (swirl :id) query-string))
       )
     (handle-website-creation url user nil query-string))
    )
  ([tmdb-id user query-string]
   (handle-tv-creation tmdb-id user nil query-string)))

(defn handle-reswirl-creation [swirl-id author]
  (let [swirl (lookups/get-swirl swirl-id)
        review  "<p data-ph=\"Why should your friends see this?\"></p>"
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

(defn search-music-page [search-term query-string]
  (let [search-result (itunes/search-albums search-term query-string)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "Album or Song" :query-string query-string})))

(defn search-books-page [search-term query-string]
  (let [search-result (amazon/search-books search-term query-string)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "Book title or author" :query-string query-string})))

(defn search-games-page [search-term query-string]
  (let [search-result (amazon/search-games search-term query-string)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "Game title" :query-string query-string})))

(defn search-movies-page [search-term query-string]
  (let [search-result (tmdb/search-movies search-term query-string)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "Movie name" :query-string query-string})))

(defn search-tv-page [search-term query-string]
  (let [search-result (tmdb/search-tv search-term query-string)]
    (layout/render "swirls/search.html" {:search-term            search-term :search-result search-result
                                         :search-box-placeholder "TV show title" :query-string query-string})))


(defn asin-from-url [url]
  (let [[_ result] (re-find #"/([0-9A-Z]{10})(?:[/?]|$)" url)]
    result))

(defn handle-itunes-creation [url user _ query-string]
  (handle-album-creation (itunes-id-from-url (str url)) user query-string))

(defn handle-amazon-creation [url user _ query-string]
  (handle-book-creation (asin-from-url (str url)) user query-string))

(defn handle-tmdb-creation [url user _ query-string]
  (handle-movie-creation (tmdb-id-from-url (str url)) user query-string))

(defn handle-imdb-creation [url user _ query-string]
  (if-let [{tmdb-id :tmdb-id type :type} (tmdb/get-tmdb-id-from-imdb-id (imdb-id-from-url (str url)))]
    (case type
      "movie" (handle-movie-creation tmdb-id user url query-string)
      "tv" (handle-tv-creation tmdb-id user url query-string)
      (handle-website-creation url user nil query-string))
    (handle-website-creation url user nil query-string)))

(defn handler-for [url]
  (let [host (.getHost url)]
    (cond (host-ends-with host "amazon.com") handle-amazon-creation
          (host-ends-with host "itunes.apple.com") handle-itunes-creation
          (host-ends-with host "themoviedb.org") handle-tmdb-creation
          (host-ends-with host "imdb.com") handle-imdb-creation
          :else handle-website-creation)
    ))

(defn handle-creation-from-url [url title author query-string]
  (try
    ((handler-for url) url author title query-string)
    (catch Exception e
      (log/warn (str "Error while handling " url author title query-string " so will fall back to generic handler. Error was") e)
      (handle-website-creation url author title query-string))))

(defn create-from-url-handler [url title req query-string]
  (let [uri (URI. url)]
    (if (= "chrome" (.getScheme (URI. url)))
      (redirect "/")
      (guard/requires-login #(handle-creation-from-url uri title (session-from req) query-string)))))


(defroutes creation-routes
           (GET "/swirls/start" [:as req] (start-page req))
           (GET "/search/music" [search-term query-string] (search-music-page search-term query-string))
           (GET "/search/books" [search-term query-string] (search-books-page search-term query-string))
           (GET "/search/movies" [search-term query-string] (search-movies-page search-term query-string))
           (GET "/search/games" [search-term query-string] (search-games-page search-term query-string))
           (GET "/search/tv" [search-term query-string] (search-tv-page search-term query-string))

           (GET "/create/from-url" [url title query-string :as req] (create-from-url-handler url title req query-string))
           (GET "/create/album" [itunes-album-id :as req] (guard/requires-login #(handle-album-creation itunes-album-id (session-from req) (req :query-string))))
           (GET "/create/book" [book-id :as req] (guard/requires-login #(handle-book-creation book-id (session-from req) (req :query-string))))
           (GET "/create/game" [game-id :as req] (guard/requires-login #(handle-game-creation game-id (session-from req) (req :query-string))))
           (GET "/create/movie" [tmdb-id :as req] (guard/requires-login #(handle-movie-creation tmdb-id (session-from req) (req :query-string))))
           (GET "/create/tv" [tmdb-id :as req] (guard/requires-login #(handle-tv-creation tmdb-id (session-from req) (req :query-string))))
           (GET "/create/reswirl" [id :as req] (guard/requires-login #(handle-reswirl-creation (Long/parseLong id) (session-from req))))
           )