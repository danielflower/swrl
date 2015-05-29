(ns yswrl.swirls.website
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]
            [yswrl.links :as links]
            [clojure.tools.logging :as log])
  (:import (java.io StringReader)))

(defn fetch-url [url]
  (html/html-resource (StringReader. (try ( :body (client/get (str url)))
                                          (catch Exception e
                                            (log/warn "Getting website failed" url e)
                                            "")))))

(defn get-content-from-resource [resource property]
  (:content (:attrs (first (filter (fn [r] (= property (:property (r :attrs)))) resource)))))

(defn nil-if-blank [str]
  (if (clojure.string/blank? str)
    nil
    str))

(defn get-metadata [url]
  (let [resource (fetch-url url)
        resource-meta (html/select resource [:meta])
        resource-title (html/text (first (html/select resource [:title])))]
    {:title ( or (nil-if-blank(get-content-from-resource resource-meta "og:title")) (nil-if-blank resource-title) nil)
     :site-name (get-content-from-resource resource-meta "og:site_name")
     :image-url (get-content-from-resource resource-meta "og:image")
     :description (get-content-from-resource resource-meta "og:description")
     }
    )
  )
