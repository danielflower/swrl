(ns yswrl.layout
  (:require [selmer.parser :as parser]
            [selmer.filters :as filters]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [environ.core :refer [env]]
            [yswrl.constraints :refer [constraints]]
            [yswrl.links :as links]))

(parser/set-resource-path! (clojure.java.io/resource "templates"))

(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :absoluteurl links/absolute)
(filters/add-filter! :swirlurl links/swirl)
(filters/add-filter! :swirlediturl links/edit-swirl)
(filters/add-filter! :img (fn [src] (if (nil? src) "" (str "<img src=\"" src "\">"))))
(filters/add-filter! :passwordreseturl links/password-reset)

(deftype RenderableTemplate [template params]
  Renderable
  (render [this request]
    (content-type
      (->> (assoc params
             :page template
             :dev (env :dev)
             :csrf-token *anti-forgery-token*
             :user (get(get request :session) :user)
             :constraints constraints
             )
           (parser/render-file (str template))
           response)
      "text/html; charset=utf-8")))

(defn render [template & [params]]
  (RenderableTemplate. template params))

