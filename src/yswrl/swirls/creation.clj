(ns yswrl.swirls.creation
  (:require [yswrl.links :as links]
            [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [compojure.core :refer [defroutes GET POST]]
            [clj-http.client :as client]
            [ring.util.response :refer [redirect response not-found]]))


(def youtube-api-key
  "AIzaSyCuxJgvMSqJbJxVYAUOINsoTjs2DuFsLMg")

(defn start-page []
  (layout/render "swirls/start.html"))



(defn session-from [req] (:user (:session req)))

(defn handle-youtube-creation [youtube-url author]
  (let [youtube-id (get (ring.util.codec/form-decode (.getQuery (java.net.URI/create youtube-url))) "v")
        youtube-result-set (:body (client/get (str "https://www.googleapis.com/youtube/v3/videos?part=snippet%2Cplayer&id=" youtube-id "&key=" youtube-api-key) {:accept :json :as :json}))
        video-info (first (youtube-result-set :items))
        title (get-in video-info [:snippet :title])
        thumbnail-url (get-in video-info [:snippet :thumbnails :default :url])
        iframe-html (get-in video-info [:player :embedHtml])
        review (str "<p>Check this out:</p>" iframe-html "<p>What do you think?</p>")]
    (let [swirl (repo/save-draft-swirl (author :id) title review thumbnail-url)]
      (redirect (links/edit-swirl (swirl :id))))))

(defn search-albums [search-term]
  (let [encoded (links/url-encode search-term)
        url (str "https://itunes.apple.com/search?term=" encoded "&media=music&entity=album")
        result (client/get url {:accept :json :as :json})]
    (result :body))
  )

(defn get-itunes-album [itunes-collection-id]
  (let [url (str "https://itunes.apple.com/lookup?id=" itunes-collection-id "&entity=song")
        result (client/get url {:accept :json :as :json})
        body (result :body)
        album (first (body :results))]
    {:title       (album :collectionName)
     :artist-name (album :artistName)
     :thumbnail-url "/blah.jpg"
     :tracks      (map (fn [r] {:track-name (r :trackName)}) (rest (body :results)))}
    ))

(defn handle-album-creation [itunes-collection-id user]
  (let [album (get-itunes-album itunes-collection-id)
        title (album :title)
        thumbnail-url (album :thumbnail-url)
        review (str "<p>Check this out:</p>" album "<p>What do you think?</p>")]
    (let [swirl (repo/save-draft-swirl (user :id) title review thumbnail-url)]
      (redirect (links/edit-swirl (swirl :id))))))

(defroutes creation-routes
           (GET "/swirls/start" [] (start-page))

           (POST "/create/youtube" [youtube-url :as req] (handle-youtube-creation youtube-url (session-from req)))
           (POST "/create/album" [itunes-album-id :as req] (handle-album-creation itunes-album-id (session-from req))))

