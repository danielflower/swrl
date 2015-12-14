(ns yswrl.home.home-routes
  (:require [yswrl.layout :as layout]
            [yswrl.links :as linky]
            [compojure.core :refer [defroutes GET]]
            [yswrl.swirls.lookups :as lookups]))



(defn home-page [count user]
  (let [max-swirls-to-get 500
        swirls-per-page 20]
    (if (nil? user)
      (let [swirls (lookups/get-all-swirls max-swirls-to-get count nil)]
        (layout/render "home/home-not-logged-in.html" {:swirls            (take swirls-per-page swirls)
                                                       :more-swirls       (nthrest swirls swirls-per-page)
                                                       :paging-url-prefix "/?from="
                                                       :return-url        "/"
                                                       :countFrom         (str count) :countTo (+ count swirls-per-page)}))
      (let [swirls (lookups/get-all-swirls-not-responded-to max-swirls-to-get count user)]
        (layout/render "home/home-logged-in.html" {:swirls            (take swirls-per-page swirls)
                                                   :more-swirls       (nthrest swirls swirls-per-page)
                                                   :paging-url-prefix "/?from="
                                                   :countFrom         (str count) :countTo (+ count swirls-per-page)})))))

(defn bookmarklet []
  (str "javascript:(function(){location.href='" (linky/url-encode (linky/absolute "/create/from-url?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title);}());"))))

(defn bookmarklet-page []
  (layout/render "home/bookmarklet.html" {:bookmarklet (bookmarklet)}))


(defn session-from [req] (:user (:session req)))

(defroutes home-routes
           (GET "/" [from :as req] (home-page (Long/parseLong (if (clojure.string/blank? from) "0" from)) (session-from req)))
           (GET "/how-to-add" [] (bookmarklet-page)))