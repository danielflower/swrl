(ns yswrl.routes.swirls
  (:require [yswrl.layout :as layout]
            [yswrl.db.swirls-repo :as repo]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect response not-found]]))


(defn create-swirl-page [who subject review error]
  (layout/render "swirls/create.html" {:who who :subject subject :review review :error error}))

(defn handle-create-swirl [who subject review {session :session}]
  (let [authorId (:id (:user session))]
    (let [swirl (repo/create-swirl authorId subject review)]
      (redirect (str "/swirls/" (:id swirl))))))

(defn view-swirl-page [id]
  (let [swirl (repo/get-swirl id)]
    (if (nil? swirl)
      (not-found nil)                                       ; how to give human readable response on 404?
      (layout/render "swirls/view.html" {:swirl swirl}))))

(defroutes swirl-routes
           (GET "/swirls/create" [_] (create-swirl-page "" "" "" nil))
           (POST "/swirls/create" [who subject review :as req] (handle-create-swirl who subject review req))
           (GET "/swirls/:id" [id] (view-swirl-page (Integer/parseInt id))))