(ns yswrl.swirls.creation
  (:require [yswrl.links :as links]
            [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [compojure.core :refer [defroutes GET POST]]
            [clj-http.client :as client]
            [ring.util.response :refer [redirect response not-found]]
            [yswrl.swirls.itunes :as itunes]))
            [clojure.xml :as xml]
            [clj-time.core :as t]
            [buddy.core.mac.hmac :as hmac]
            [buddy.core.codecs :as codecs]
            [ring.util.response :refer [redirect response not-found]])
  (:import (javax.crypto.spec SecretKeySpec)))


(def youtube-api-key
  "AIzaSyCuxJgvMSqJbJxVYAUOINsoTjs2DuFsLMg")

(def amazon-key "tDwOEE+vRplK8EhmRhrt8BIuxZi1NvSYbmpwxTv5")

(defn start-page []
  (layout/render "swirls/start.html"))

(defn session-from [req] (:user (:session req)))

(defn params [bookname] (sorted-map
                          :AWSAccessKeyId "AKIAIO3J752UN7X4HUWA"
                          :AssociateTag "corejavaint0d-20"
                          :Keywords bookname
                          :Operation "ItemSearch"
                          :ResponseGroup "Images,ItemAttributes"
                          :SearchIndex "Books"
                          :Service "AWSECommerceService"
                          :Timestamp (str (t/now))
                          :Version "2011-08-01"
                          ))

(defn string-to-sign [pms]
  (str "GET\nwebservices.amazon.com\n/onca/xml\n"
       (ring.util.codec/form-encode pms)))

(defn youtube-id [url]
  (get (ring.util.codec/form-decode (.getQuery (java.net.URI/create url))) "v"))

(defn sign [key string]
  "Returns the signature of a string with a given
    key, using a SHA-256 HMAC."
  (-> (hmac/hash string key :sha256)
      (codecs/bytes->base64))
  )


(defn createEncryptedUrl [paz]
  (str "http://webservices.amazon.com/onca/xml?"
       (ring.util.codec/form-encode paz) "&Signature="
       (ring.util.codec/form-encode
         (sign amazon-key
               (string-to-sign paz)))))

(defn url-to-call [bookname]
  (createEncryptedUrl (params bookname)))

(defn handle-amazon [bookname]
  (clojure.zip/xml-zip(xml/parse (url-to-call bookname))))

(defn handle-amazon-creation [bookname author]
  (let [SO
        (xml/parse (:body (client/get
                            (url-to-call bookname))))]))


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
  (let [youtube-id (youtube-id youtube-url)
        info (get-video-details youtube-id)
        swirl (repo/save-draft-swirl (author :id) (info :title) (info :review) (info :thumbnail-url))]
    (redirect (links/edit-swirl (swirl :id)))))


(defn handle-album-creation [itunes-collection-id user]
  (let [album (itunes/get-itunes-album itunes-collection-id)
        title (album :title)
        thumbnail-url (album :thumbnail-url)
        track-html (clojure.string/join (map #(str "<li>" (% :track-name) "</li>") (album :tracks)))
        review (str "<img src=\"" thumbnail-url "\"><p>Track listing:</p><ol>" track-html "</ol><p>What do you think?</p>")]
    (let [swirl (repo/save-draft-swirl (user :id) title review thumbnail-url)]
      (redirect (links/edit-swirl (swirl :id))))))



(defn search-music-page [search-term]
  (let [search-result (itunes/search-albums search-term)]
    (layout/render "swirls/search.html" {:search-term search-term :search-result search-result})))

(defroutes creation-routes
           (GET "/swirls/start" [] (start-page))
           (GET "/search/music" [search-term] (search-music-page search-term))

           (POST "/create/youtube" [youtube-url :as req] (handle-youtube-creation youtube-url (session-from req)))
           (GET "/create/album" [itunes-album-id :as req] (handle-album-creation itunes-album-id (session-from req))))

           (POST "/create/amazon" [book-name :as req] (handle-amazon-creation book-name (session-from req)))
           (POST "/create/youtube" [youtube-url :as req] (handle-youtube-creation youtube-url (session-from req))))