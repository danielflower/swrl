(ns yswrl.routes.swirls
  (:require [yswrl.layout :as layout]
            [yswrl.db.swirls-repo :as repo]
            [yswrl.db.auth-repo :as user-repo]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect response not-found]]))
(use '[clojure.string :only [split]])

(defn create-swirl-page [who subject review error]
  (layout/render "swirls/create.html" {:who who :subject subject :review review :error error}))

(defn handle-create-swirl [who subject review {session :session}]
  (let [authorId (:id (:user session))]
    (let [swirl (repo/create-swirl authorId subject review (split who #","))]
      (redirect (str "/swirls/" (:id swirl))))))

(defn view-swirl-page [id]
  (let [swirl (repo/get-swirl id)]
    (if (nil? swirl)
      (not-found nil)                                       ; how to give human readable response on 404?
      (layout/render "swirls/view.html" {:swirl swirl}))))

(defn view-swirls-by [authorName]
  (if-let [author (user-repo/get-user authorName)]
    (if-let [swirls (repo/get-swirls-authored-by ( :id author))]
      (layout/render "swirls/list.html" { :pageTitle (str "Reviews by " (author :username)) :author author :swirls swirls}))))

(defroutes swirl-routes
           (GET "/swirls/create" [_] (create-swirl-page "" "" "" nil))
           (POST "/swirls/create" [who subject review :as req] (handle-create-swirl who subject review req))
           (GET "/swirls/:id" [id] (view-swirl-page (Integer/parseInt id)))
           (GET "/swirls/by/:authorName" [authorName] (view-swirls-by authorName)))