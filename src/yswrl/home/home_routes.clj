(ns yswrl.home.home-routes
  (:require [yswrl.layout :as layout]
            [yswrl.links :as linky]
            [compojure.core :refer [defroutes GET]]
            [clojure.tools.logging :as log]
            [yswrl.user.notifications :as notifications]))

(defn send-notifications-because-we-do-not-have-a-scheduler
  []
  (try
    (notifications/send-pending-notifications)
    (catch Exception e
      (log/error "Error while sending notifications" e))))


(defn home-page []
  (send-notifications-because-we-do-not-have-a-scheduler)
  (layout/render "home/home.html"))

(defn bookmarklet []
  (str "javascript:(function(){location.href='" (linky/url-encode (linky/absolute "/create/from-url?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title);}());")) ))

(defn bookmarklet-page []
  (layout/render "home/bookmarklet.html" { :bookmarklet (bookmarklet) }))


(defroutes home-routes
           (GET "/" [] (home-page))
           (GET "/how-to-add" [] (bookmarklet-page)))