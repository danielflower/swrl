(ns yswrl.home.home-routes
  (:require [yswrl.layout :as layout]
            [yswrl.links :as linky]
            [compojure.core :refer [defroutes GET]]))

(defn home-page []
  (layout/render "home/home.html"))

(defn bookmarklet-page []
  (layout/render "home/bookmarklet.html" { :bookmarklet (str "javascript:(function(){location.href='" (linky/url-encode (linky/absolute "/create/from-url?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title);}());")) ) }))


(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/how-to-add" [] (bookmarklet-page)))