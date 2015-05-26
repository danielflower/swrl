(ns yswrl.swirls.website
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]
            [yswrl.links :as links])
  (:import (java.io StringReader)))

(defn fetch-url [url]
  (html/html-resource (StringReader.  ( :body (client/get (str url))))))

(defn get-content-from-resource [resource property]
  (:content (:attrs (first (filter (fn [r] (= property (:property (r :attrs)))) resource)))))

(defn get-metadata [url]
  (let [resource (fetch-url url)
        resource-meta (html/select resource [:meta])
        resource-title (html/text (first (html/select resource [:title])))]
    {:title ( or (get-content-from-resource resource-meta "og:title") resource-title)
     :site-name (get-content-from-resource resource-meta "og:site_name")
     :image-url (get-content-from-resource resource-meta "og:image")
     :description (get-content-from-resource resource-meta "og:description")
     }
    )
  )
