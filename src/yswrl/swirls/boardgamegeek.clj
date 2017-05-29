(ns yswrl.swirls.boardgamegeek
  (:use [clojure.data.zip.xml :only (attr attr= text xml-> xml1->)])
  (:require
    [clj-http.client :as client]
    [clojure.data.xml :as xml-data]
    [clojure.zip :as zip]
    [clojure.string :as string]))

(defn get-raw-results [search-term]
  (-> (client/get (str "https://www.boardgamegeek.com/xmlapi/search?search=" search-term))
      :body))

(defn get-raw-details [bgg-id]
  (-> (client/get (str "https://www.boardgamegeek.com/xmlapi/boardgame/" bgg-id))
      :body))

(defn raw-details-to-swrl-map [raw-results]
  {:url             (str "https://boardgamegeek.com/boardgame/"
                         (first (xml-> raw-results :boardgame (attr :objectid))))
   :title           (apply str (xml-> raw-results :boardgame :name (attr= :primary "true") text))
   :overview        (apply str (xml-> raw-results :boardgame :description text))
   :categories      (xml-> raw-results :boardgame :boardgamecategory text)
   :bgg-id          (first (xml-> raw-results :boardgame (attr :objectid)))
   :designer        (string/join ", " (xml-> raw-results :boardgame :boardgamedesigner text))
   :thumbnail-url   (str "https:" (apply str (xml-> raw-results :boardgame :image text)))
   :large-image-url (str "https:" (apply str (xml-> raw-results :boardgame :image text)))})

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
       {:results (map raw-details-to-swrl-map detailed-results)}
       )))
  ([search-term]
   (search search-term "")))

(defn get-by-id [id]
  (let [details (-> (get-raw-details id)
                    xml-data/parse-str
                    zip/xml-zip)]
    (raw-details-to-swrl-map details)))