(ns yswrl.swirls.swirl-routes
  (:require [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.auth.auth-repo :as user-repo]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect response not-found]]))

(defn create-swirl-page [who subject review error]
  (layout/render "swirls/create.html" {:who who :subject subject :review review :error error}))

(defn handle-create-swirl [who subject review {session :session}]
  (let [authorId (:id (:user session))
        namesOrEmails (map (fn [x] (clojure.string/trim x)) (clojure.string/split who #","))
        swirl (repo/create-swirl authorId subject review namesOrEmails)]
    (redirect (str "/swirls/" (:id swirl)))))

(defn view-swirl-page [id]
  (if-let [swirl (repo/get-swirl id)]
    (let [responses (repo/get-swirl-responses (:id swirl))
          comments (repo/get-swirl-comments (:id swirl))]
      (layout/render "swirls/view.html" {:swirl swirl :responses responses :comments comments}))))

(defn view-swirls-by [authorName]
  (if-let [author (user-repo/get-user authorName)]
    (if-let [swirls (repo/get-swirls-authored-by (:id author))]
      (layout/render "swirls/list.html" {:pageTitle (str "Reviews by " (author :username)) :author author :swirls swirls}))))

(defn session-from [req] (:user (:session req)))

(defn handle-response [swirl-id summary author]
  (do
    (repo/create-response swirl-id summary author)
    (redirect (str "/swirls/" swirl-id))))

(defn handle-comment [swirl-id comment author]
  (do
    (repo/create-comment swirl-id comment author)
    (redirect (str "/swirls/" swirl-id))))

(defroutes swirl-routes
           (GET "/swirls/create" [_] (create-swirl-page "" "" "" nil))
           (POST "/swirls/create" [who subject review :as req] (handle-create-swirl who subject review req))
           (GET "/swirls/:id" [id] (view-swirl-page (Integer/parseInt id)))
           (POST "/swirls/:id/respond" [id response-summary :as req] (handle-response (Integer/parseInt id) response-summary (session-from req)))
           (POST "/swirls/:id/comment" [id comment :as req] (handle-comment (Integer/parseInt id) comment (session-from req)))
           (GET "/swirls/by/:authorName" [authorName] (view-swirls-by authorName)))