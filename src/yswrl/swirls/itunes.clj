(ns yswrl.swirls.itunes
  (:require [yswrl.links :as links]
            [clj-http.client :as client]))


(defn search-albums
  ([search-term origin-swirl-id]
  (if (clojure.string/blank? search-term)
    {:results []}
    (let [encoded (links/url-encode search-term)
          url (str "https://itunes.apple.com/search?term=" encoded "&media=music&entity=album")
          result (client/get url {:accept :json :as :json})] {
                                                              :results (map (fn [r] {:type          (r :collectionType)
                                                                                     :title         (r :collectionName)
                                                                                     :artist        (r :artistName)
                                                                                     :create-url    (str "/create/album?itunes-album-id=" (r :collectionId)
                                                                                                        (if (clojure.string/blank? origin-swirl-id)
                                                                                                           nil
                                                                                                           (str "&origin-swirl-id=" origin-swirl-id)))
                                                                                     :itunes-id     (r :collectionId)
                                                                                     :thumbnail-url (r :artworkUrl60)}) ((result :body) :results))
                                                              })))
  ([search-term]
   (search-albums search-term ""))
  )

(defn get-itunes-album [itunes-collection-id]
  (let [url (str "https://itunes.apple.com/lookup?id=" itunes-collection-id "&entity=song")
        result (client/get url {:accept :json :as :json})
        body (result :body)
        album (first (body :results))]
    {:title         (album :collectionName)
     :artist-name   (album :artistName)
     :thumbnail-url (album :artworkUrl100)
     :tracks        (map (fn [r] {:track-name (r :trackName)
                                  :title      (r :collectionName)}) (rest (body :results)))}
    ))
