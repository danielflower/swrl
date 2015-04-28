(ns yswrl.home.home-routes
  (:require [yswrl.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render "home/home.html"))


(defroutes home-routes
  (GET "/" [] (home-page)))