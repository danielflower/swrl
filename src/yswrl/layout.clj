(ns yswrl.layout
  (:require [selmer.parser :as parser]
            [selmer.filters :as filters]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [environ.core :refer [env]]
            [yswrl.constraints :refer [constraints]]
            [yswrl.links :as links]
            [yswrl.swirls.lookups :as lookups]
            [yswrl.auth.auth-repo :as auth-repo]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [yswrl.user.notifications-repo :as notifications-repo]))

(def response-icons {
                     "loved it" "fa-heart"
                     "meh" "fa-meh-o"
                     "not bad" "fa-thumbs-up"
                     "ha" "fa-smile-o"
                     "haha" "fa-smile-o"
                     "later" "fa-clock-o"
                     "not for me" "fa-times"
                     "purchased" "fa-usd"
                     "wtf" "fa-question"
                     "um" "fa-question"
                     })

(def iso-date-formatter (f/formatter "yyyy-MM-dd'T'HH:mmZ"))
(def human-friendly-date-formatter (f/formatter "dd MMM yyyy"))

(parser/set-resource-path! (clojure.java.io/resource "templates"))

(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :absoluteurl links/absolute)
(filters/add-filter! :swirlurl links/swirl)
(filters/add-filter! :swirlediturl links/edit-swirl)
(filters/add-filter! :swirldeleteurl links/delete-swirl)
(filters/add-filter! :itunesalbum links/itunes-album)
(filters/add-filter! :inboxlink links/inbox)
(filters/add-filter! :user-url links/user)
(filters/add-filter! :response-icon #(get response-icons (clojure.string/lower-case %) "fa-star"))
(filters/add-filter! :img (fn [src] (if (nil? src) "" (str "<img src=\"" src "\">"))))
(filters/add-filter! :timetag (fn [javaDate]
                             (let [date (c/from-date javaDate)]
                               [:safe (str "<time datetime=\"" (f/unparse iso-date-formatter date) "\">" (f/unparse human-friendly-date-formatter date) "</time>")])))
(filters/add-filter! :passwordreseturl links/password-reset)

(filters/add-filter! :gravatar-img (fn [email-hash size] [:safe (str "<img class=\"gravatar\" src=\"" (links/gravatar-url email-hash size) "\" width=\"" size "\" height=\"" size "\" alt=\"\">")]))

(deftype RenderableTemplate [template params]
  Renderable
  (render [this request]
    (let [current-user (get (get request :session) :user)
          unread-count (if current-user (lookups/get-swirls-awaiting-response-count (get current-user :id nil)) nil)
          response-counts (if current-user (lookups/get-response-count-for-user (get current-user :id -1)) nil)
          notifications-count (if current-user (count (notifications-repo/get-for-user-email (get current-user :id nil))) nil)]

      (content-type
        (->> (assoc params
               :page template
               :dev (env :dev)
               :csrf-token *anti-forgery-token*
               :user (if (nil? current-user) nil (auth-repo/get-user (current-user :username))) ; todo lookup by remember-me token
               :unread-count unread-count
               :response-counts response-counts
               :notifications-count notifications-count
               :constraints constraints
               :request request
               )
             (parser/render-file template)
             response)
        "text/html; charset=utf-8"))))

(defn render [template & [params]]
  (RenderableTemplate. template params))

(defn render-string [template-path model]
  (parser/render-file template-path model))