(ns yswrl.swirls.boardgamegeek
  (:use [clojure.data.zip.xml :only (attr attr= text xml-> xml1->)])
  (:require
    [clj-http.client :as client]
    [clojure.data.xml :as xml-data]
    [clojure.zip :as zip]))

(defn get-raw-results [search-term]
  (-> (client/get (str "https://www.boardgamegeek.com/xmlapi/search?search=" search-term))
      :body))

(defn get-raw-details [bgg-id]
  (-> (client/get (str "https://www.boardgamegeek.com/xmlapi/boardgame/" bgg-id ))
      :body))

(defn search
  ([search-term query-string]
   (if (clojure.string/blank? search-term)
     {:results []}
     (let [result (get-raw-results search-term)
           parsed-result (-> result
                             xml-data/parse-str
                             zip/xml-zip)
           result-ids (mapv (fn [xml]
                             (-> xml
                                 :attrs
                                 :objectid))
                           (-> parsed-result
                               zip/node
                               :content))
           detailed-results (map (fn [id]
                                    (-> (get-raw-details id)
                                        xml-data/parse-str
                                        zip/xml-zip))
                                  result-ids)]
       {:results (map (fn [r]
                        {:url             (str "https://boardgamegeek.com/boardgame/"
                                               (first (xml-> r :boardgame (attr :objectid))))
                         :title           (apply str (xml-> r :boardgame :name (attr= :primary "true") text))
                         :overview        (apply str (xml-> r :boardgame :description text))
                         :categories      (xml-> r :boardgame :boardgamecategory text)
                         :bgg-id          (first (xml-> r :boardgame (attr :objectid)))
                         :thumbnail-url   (str "https:" (apply str (xml-> r :boardgame :image text)))
                         :large-image-url (str "https:" (apply str (xml-> r :boardgame :image text)))})
                      detailed-results)
        })))
  ([search-term]
   (search search-term "")))