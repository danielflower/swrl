(ns yswrl.home.home-routes
  (:require [yswrl.layout :as layout]
            [yswrl.links :as linky]
            [clojure.string :refer [join]]
            [compojure.core :refer [defroutes GET]]
            [yswrl.swirls.lookups :as lookups]
            [yswrl.user.notifications :as notifications]))



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
      (let [public-swirls (lookups/get-all-swirls-not-responded-to 200 from user)
            recommended-swirls (lookups/get-swirls-awaiting-response user 200 0)
            notifications (notifications/get-notifications-and-mark-responses-as-seen-for user)
            wishlist (filter #(or (= "wishlist" (:state %)) (= "consuming" (:state %))) (lookups/get-swirls-in-user-swrl-list user 200 0 user))
            friends-swirls (lookups/get-swirls-authored-by-friends user)
            num-preview 10]
        (layout/render "home/home-logged-in.html" {:public-swirls               (take num-preview public-swirls)
                                                   :more-public-swirls-url      "/swirls"
                                                   :recommended-swirls          recommended-swirls
                                                   :friends-swirls              friends-swirls
                                                   :wishlist                    (take 20 wishlist)
                                                   :more-swirls                 (join "," (map :id (nthrest wishlist swirls-per-page)))
                                                   :paging-url-prefix           "/?from="
                                                   :swirls-per-page             swirls-per-page
                                                   :countFrom                   (str from)
                                                   :countTo                     (+ from swirls-per-page)
                                                   :notifications               notifications})))))

(defn bookmarklet []
  (str "javascript:(function(){location.href='" (linky/url-encode (linky/absolute "/create/from-url?url='+encodeURIComponent(location.href)+'&title='+encodeURIComponent(document.title);}());"))))

(defn bookmarklet-page []
  (layout/render "home/bookmarklet.html" {:title "Help page" :bookmarklet (bookmarklet)}))


(defn session-from [req] (:user (:session req)))

(defroutes home-routes
           (GET "/" [from :as req] (home-page (Long/parseLong (if (clojure.string/blank? from) "0" from)) (session-from req)))
           (GET "/how-to-add" [] (bookmarklet-page)))