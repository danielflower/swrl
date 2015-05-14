(ns yswrl.swirls.itunes
  (:require [yswrl.links :as links]
            [clj-http.client :as client]))


(defn search-albums [search-term]
  (let [encoded (links/url-encode search-term)
        url (str "https://itunes.apple.com/search?term=" encoded "&media=music&entity=album")
        result (client/get url {:accept :json :as :json})]
    {
     :results (map (fn [r] {:type  (r :collectionType)
                            :title (r :collectionName)
                            :artist (r :artistName)
                            :itunes-id (r :collectionId)
                            :thumbnail-url (r :artworkUrl100)}) ((result :body) :results))
     }
    )
  )

(defn get-itunes-album [itunes-collection-id]
  (let [url (str "https://itunes.apple.com/lookup?id=" itunes-collection-id "&entity=song")
        result (client/get url {:accept :json :as :json})
        body (result :body)
        album (first (body :results))]
    {:title         (album :collectionName)
     :artist-name   (album :artistName)
     :thumbnail-url "/blah.jpg"
     :tracks        (map (fn [r] {:track-name (r :trackName)
                                  :title      (r :collectionName)}) (rest (body :results)))}
    ))
