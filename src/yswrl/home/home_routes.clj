(ns yswrl.home.home-routes
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET]]))

(defn home-page []
  (layout/render "home/home.html"))


(defroutes home-routes
  (GET "/" [] (home-page)))