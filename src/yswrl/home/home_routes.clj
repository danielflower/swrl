(ns yswrl.home.home-routes
  (:require [yswrl.layout :as layout]
            [yswrl.links :as linky]
            [compojure.core :refer [defroutes GET]]
            [yswrl.swirls.lookups :as lookups]))



(defn home-page [user]
  (if (nil? user)
    (layout/render "home/home-not-logged-in.html" {:swirls (lookups/get-all-swirls 20 0)})
    (layout/render "home/home-logged-in.html" {:swirls (lookups/get-all-swirls 20 0)})))

(defn bookmarklet []
  (str "javascript:(function(){location.href='" (linky/url-encode (linky/absolute "/create/from-url?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title);}());"))))

(defn bookmarklet-page []
  (layout/render "home/bookmarklet.html" {:bookmarklet (bookmarklet)}))


(defn session-from [req] (:user (:session req)))

(defroutes home-routes
           (GET "/" [:as req] (home-page (session-from req)))
           (GET "/how-to-add" [] (bookmarklet-page)))