(ns yswrl.swirls.creation
  (:require [yswrl.links :as links]
            [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [compojure.core :refer [defroutes GET POST]]
            [clj-http.client :as client]
            [ring.util.response :refer [redirect response not-found]]
            [yswrl.swirls.itunes :as itunes]
            [yswrl.swirls.amazon :as amazon]
            [ring.util.response :refer [redirect response not-found]]
            [yswrl.auth.guard :as guard])
  (:import (java.net URL)))


(def youtube-api-key
  "AIzaSyCuxJgvMSqJbJxVYAUOINsoTjs2DuFsLMg")

(defn start-page []
  (layout/render "swirls/start.html"))

(defn session-from [req] (:user (:session req)))

(defn youtube-id [url]
  (get (ring.util.codec/form-decode (.getQuery (java.net.URI/create url))) "v"))



(defn get-video-details [youtube-id]
  (let [url (str "https://www.googleapis.com/youtube/v3/videos?part=snippet%2Cplayer&id=" youtube-id "&key=" youtube-api-key)
        youtube-result-set (:body (client/get url {:accept :json :as :json}))
        video-info (first (youtube-result-set :items))
        title (get-in video-info [:snippet :title])
        thumbnail-url (get-in video-info [:snippet :thumbnails :default :url])
        iframe-html (get-in video-info [:player :embedHtml])
        review (str "<p>Check this out:</p>" iframe-html "<p>What do you think?</p>")]
    {:title         title
     :thumbnail-url thumbnail-url
     :iframe-html   iframe-html
     :review        review}))

(defn handle-youtube-creation [youtube-url author]
  (let [youtube-id (youtube-id (str youtube-url))
        info (get-video-details youtube-id)
        swirl (repo/save-draft-swirl "youtube" (author :id) (info :title) (info :review) (info :thumbnail-url), {})]
    (redirect (links/edit-swirl (swirl :id)))))

(defn handle-website-creation [url author]
  (let [swirl (repo/save-draft-swirl "website" (author :id) "This website" (str "Check out <a href=\"" url "\">" url "</a>") nil, {})]
    (redirect (links/edit-swirl (swirl :id)))))

(defn handler-for [url]
  (let [host (.getHost url)]
    (cond (or (= host "youtube.com") (.endsWith host ".youtube.com")) handle-youtube-creation
      :else handle-website-creation)
  ))

(defn handle-creation-from-url [url author]
  ((handler-for url) url author))

(defn handle-album-creation [itunes-collection-id user]
  (let [album (itunes/get-itunes-album itunes-collection-id)
        title (album :title)
        thumbnail-url (album :thumbnail-url)
        track-html (clojure.string/join (map #(str "<li>" (% :track-name) "</li>") (album :tracks)))
        review (str "<img src=\"" thumbnail-url "\"><p>Track listing:</p><ol>" track-html "</ol><p>What do you think?</p>")]
    (let [swirl (repo/save-draft-swirl "album" (user :id) title review thumbnail-url, {:itunes-collection-id (Long/parseLong itunes-collection-id)})]
      (redirect (links/edit-swirl (swirl :id))))))

(defn handle-book-creation [asin user]
  (let [book (amazon/get-book asin)
        title (str (book :title) " by " (book :author))
        big-img-url (book :big-img-url)
        book-html (book :blurb)
        url (book :url)
        review (str "<img src=\"" big-img-url "\"><p><a href=\"" url "\">Buy now from Amazon</a><p>Blurb:</p>" book-html "<p>What do you think?</p>")]
    (let [swirl (repo/save-draft-swirl "book" (user :id) title review big-img-url, {})]
      (redirect (links/edit-swirl (swirl :id))))))



(defn search-music-page [search-term]
  (let [search-result (itunes/search-albums search-term)]
    (layout/render "swirls/search.html" {:search-term search-term :search-result search-result})))

(defn search-books-page [search-term]
  (let [search-result (amazon/search-books search-term)]
    (println search-result)
    (layout/render "swirls/search_books.html" {:search-term search-term :search-result search-result})))

(defroutes creation-routes
           (GET "/swirls/start" [] (start-page))
           (GET "/search/music" [search-term] (search-music-page search-term))
           (GET "/search/books" [search-term] (search-books-page search-term))

           (GET "/create/from-url" [url :as req] (guard/requires-login #(handle-creation-from-url (URL. url) (session-from req))))
           (GET "/create/album" [itunes-album-id :as req] (guard/requires-login #(handle-album-creation itunes-album-id (session-from req))))
           (GET "/create/book" [book-id :as req] (guard/requires-login #(handle-book-creation book-id (session-from req))))
           )