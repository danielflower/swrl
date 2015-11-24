(ns yswrl.swirls.website
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]
            [yswrl.swirls.types :as types]
            [clojure.tools.logging :as log])
  (:import (java.io StringReader)))

(defn fetch-url [url]
  (html/html-resource (StringReader. (try (:body (client/get (str url)))
                                          (catch Exception e
                                            (try (:body (client/get (str "http://" url)))
                                                 (catch Exception e
                                                   (try (:body (client/get (str "https://" url)))
                                                        (catch Exception e
                                                          (log/warn "Getting website failed" url e)
                                                          "")))))
                                          ))))

(defn get-content-from-resource [resource property]
  (-> (filter (fn [r] (= property (:property (r :attrs)))) resource)
      first
      :attrs
      :content))

(defn nil-if-blank [str]
  (if (clojure.string/blank? str)
    nil
    str))

(defn meta-value [resource-meta key default-value]
  (or (nil-if-blank (get-content-from-resource resource-meta key))
      default-value))

(defn embed-html-for [html]
  (if-let [raw-url (some #(meta-value html % nil) ["og:video:url" "og:video:secure_url" "og:video"])]
    (if (= "text/html" (meta-value html "og:video:type" nil))
      (let [url (clojure.string/replace (clojure.string/replace raw-url "autoPlay=1" "autoPlay=0") "autoplay=1" "autoplay=0")
            width (meta-value html "og:video:width" "640")
            height (meta-value html "og:video:height" "320")]
        (str "<iframe sandbox=\"allow-scripts allow-same-origin allow-popups\" width=\"" width "\" height=\"" height "\" src=\"" url "\" frameborder=\"0\" allowfullscreen></iframe>")))))

(defn get-metadata [url]
  (let [resource (fetch-url url)
        resource-meta (html/select resource [:meta])
        resource-title (html/text (first (html/select resource [:title])))
        type (types/from-open-graph-type (get-content-from-resource resource-meta "og:type"))]
    {:title       (meta-value resource-meta "og:title" (nil-if-blank resource-title))
     :site-name   (get-content-from-resource resource-meta "og:site_name")
     :image-url   (meta-value resource-meta "og:image" nil)
     :description (get-content-from-resource resource-meta "og:description")
     :type        type
     :embed-html  (embed-html-for resource-meta)
     }
    )
  )
