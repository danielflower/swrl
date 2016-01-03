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
            [yswrl.groups.groups-repo :as group-repo]
            [yswrl.auth.auth-repo :as auth-repo]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [yswrl.user.notifications-repo :as notifications-repo]))

(def response-icons {
                     "loved it"   "fa-heart"
                     "dismissed"  "fa-times"
                     "meh"        "fa-meh-o"
                     "not bad"    "fa-meh-o"
                     "ha"         "fa-smile-o"
                     "haha"       "fa-smile-o"
                     "later"      "fa-clock-o"
                     "not for me" "fa-times"
                     "purchased"  "fa-usd"
                     "wtf"        "fa-question"
                     "um"         "fa-question"
                     })

(def iso-date-formatter (f/formatter "yyyy-MM-dd'T'HH:mmZ"))
(def human-friendly-date-formatter (f/formatter "dd MMM yyyy"))

(defn swirl-title [id]
  (:title (lookups/get-swirl (Long/parseLong id))))

(parser/set-resource-path! (clojure.java.io/resource "templates"))

(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :absoluteurl links/absolute)
(filters/add-filter! :swirlurl links/swirl)
(filters/add-filter! :groupurl links/group)
(filters/add-filter! :joingroupurl links/join-group)
(filters/add-filter! :editgroupurl links/edit-group)
(filters/add-filter! :swirlediturl links/edit-swirl)
(filters/add-filter! :swirlediturledit #(links/edit-swirl % "edit=true"))
(filters/add-filter! :notification-options-url links/notification-options)
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

(defn generate-gravatar-img-html [email-hash size]
  (str "<img class=\"gravatar\" src=\"" (links/gravatar-url email-hash size) "\" width=\"" size "\" height=\"" size "\" alt=\"\">"))

(filters/add-filter! :gravatar-img (fn [email-hash size] [:safe (generate-gravatar-img-html email-hash size)]))

(filters/add-filter! :gravatar-img-url (fn [email-hash size] (links/gravatar-url email-hash size)))

(defn generate-user-selector-label [email-hash username]
  (str "<input id=\"" username "\" type=\"checkbox\" name=\"who\" value=\"" username "\" checked><label for=\"" username "\">" (generate-gravatar-img-html email-hash 35) "" username "</label>"))

(filters/add-filter! :user-selector-label (fn [user] (let [email-hash (:email_md5 user)
                                                           username (:username user)]
                                                       (generate-user-selector-label email-hash username))))

(filters/add-filter! :swirl-title swirl-title)
(filters/add-filter! :empty-review? (fn [review]
                                      (boolean (or (= "" review)
                                                   (re-matches #"<p data\-ph=\"[^\"]+\"><\/p>" review)))))

(selmer.filters/add-filter! :swirl-html (fn [swirl]
                                          (parser/render-file "components/mini-swirl.html" {:swirl swirl})))
(filters/add-filter! :image-search-url (fn [title]
                                         (str "https://www.google.co.uk/search?q="
                                              (links/url-encode title)
                                              "&tbm=isch&tbs=isz:l")))


(deftype RenderableTemplate [template params]
  Renderable
  (render [_ request]
    (let [current-user (get (get request :session) :user)
          unread-count (if current-user (lookups/get-swirls-awaiting-response-count current-user) nil)]

      (content-type
        (->> (assoc params
               :page template
               :dev (env :dev)
               :csrf-token *anti-forgery-token*
               :user (if (nil? current-user) nil (auth-repo/get-user (current-user :username))) ; todo lookup by remember-me token
               :groups (if (nil? current-user) nil (group-repo/get-groups-for (current-user :id)))
               :unread-count unread-count
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