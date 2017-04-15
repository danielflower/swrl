(ns yswrl.features.home-features
  (:require [yswrl.handler :refer [app]]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]))
(selmer.parser/cache-off!)

(deftest homepage-greeting
  (-> (session app)
      (visit "/")
      (within [:h2]
                (contains? (text? "Recommendations for your friends")))))

(deftest bookmarklet-page
  (-> (session app)
      (visit "/how-to-add")
      (within [:h1]
              (has (text? "Adding links")))
      (has (attr? [:.bookmarklet] :href "javascript:(function(){location.href='https%3A%2F%2Fwww.swrl.co%2Fcreate%2Ffrom-url%3Furl%3D%27%2BencodeURIComponent%28location.href%29%2B%27%26title%3D%27%2BencodeURIComponent%28document.title%29%3B%7D%28%29%29%3B"))

      ))

(deftest extension-from-chrome-page-redirects-to-homepage
  (-> (session app)
      (visit "/create/from-url?url=chrome%3A%2F%2Fnewtab%2F&title=New%20Tab")
      (follow-redirect)
      (within [:h2]
              (contains? (text? "Recommendations for your friends")))))
