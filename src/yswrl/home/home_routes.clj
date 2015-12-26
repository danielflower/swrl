(ns yswrl.home.home-routes
  (:require [yswrl.layout :as layout]
            [yswrl.links :as linky]
            [clojure.string :refer [join]]
            [compojure.core :refer [defroutes GET]]
            [yswrl.swirls.lookups :as lookups]))



(defn home-page [from user]
  (let [swirls-per-page 20]
    (if (nil? user)
      (let [swirls (lookups/get-all-swirls 100 from nil)]
        (layout/render "home/home-not-logged-in.html" {:swirls            (take swirls-per-page swirls)
                                                       :more-swirls       (join "," (map :id (nthrest swirls swirls-per-page)))
                                                       :paging-url-prefix "/swirls?from="
                                                       :return-url        "/"
                                                       :swirls-per-page   swirls-per-page
                                                       :countFrom         (str from)
                                                       :countTo           (+ from swirls-per-page)}))
      (let [swirls (lookups/get-all-swirls-not-responded-to 200 from user)]
        (layout/render "home/home-logged-in.html" {:swirls            (take swirls-per-page swirls)
                                                   :more-swirls       (join "," (map :id (nthrest swirls swirls-per-page)))
                                                   :paging-url-prefix "/?from="
                                                   :swirls-per-page   swirls-per-page
                                                   :countFrom         (str from)
                                                   :countTo           (+ from swirls-per-page)})))))

(defn bookmarklet []
  (str "javascript:(function(){location.href='" (linky/url-encode (linky/absolute "/create/from-url?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title);}());"))))

(defn bookmarklet-page []
  (layout/render "home/bookmarklet.html" {:title "Help page" :bookmarklet (bookmarklet)}))


(defn session-from [req] (:user (:session req)))

(defroutes home-routes
           (GET "/" [from :as req] (home-page (Long/parseLong (if (clojure.string/blank? from) "0" from)) (session-from req)))
           (GET "/how-to-add" [] (bookmarklet-page)))