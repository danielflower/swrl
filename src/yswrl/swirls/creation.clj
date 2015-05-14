(ns yswrl.swirls.creation
  (:require [yswrl.links :as links]
            [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [compojure.core :refer [defroutes GET POST]]
            [clj-http.client :as client]
            [clojure.data.codec.base64 :as b64]
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
  (xml/parse (:body (client/get
                      (url-to-call bookname))))
  )
(defn handle-amazon-creation [bookname author]
  (let [amazon-result-set
        (xml/parse (:body (client/get
                            (url-to-call bookname))))]))




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

(defroutes creation-routes
           (GET "/swirls/start" [] (start-page))
           (POST "/create/amazon" [book-name :as req] (handle-amazon-creation book-name (session-from req)))
           (POST "/create/youtube" [youtube-url :as req] (handle-youtube-creation youtube-url (session-from req))))