(ns yswrl.swirls.swirl-routes
  (:require [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.auth.auth-repo :as user-repo]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [redirect response not-found]]))

(defn create-swirl-page [who subject review error]
  (layout/render "swirls/create.html" {:who who :subject subject :review review :error error}))

(defn handle-create-swirl [who subject review current-user]
  (let [authorId (:id current-user)
        namesOrEmails (map (fn [x] (clojure.string/trim x)) (clojure.string/split who #","))
        swirl (repo/create-swirl authorId subject review namesOrEmails)]
    (redirect (str "/swirls/" (:id swirl)))))

(def not-nil? (complement nil?))

(defn view-swirl-page [id current-user]
  (if-let [swirl (repo/get-swirl id)]
    (let [responses (repo/get-swirl-responses (:id swirl))
          comments (repo/get-swirl-comments (:id swirl))
          can-respond (and (not-nil? current-user) (not-any? (fn [c] (= (:id current-user) (:responder c))) responses)) ]
      (layout/render "swirls/view.html" {:swirl swirl :responses responses :comments comments :can-respond can-respond}))))

(defn view-swirls-by [authorName]
  (if-let [author (user-repo/get-user authorName)]
    (if-let [swirls (repo/get-swirls-authored-by (:id author))]
      (layout/render "swirls/list.html" {:pageTitle (str "Reviews by " (author :username)) :author author :swirls swirls}))))

(defn session-from [req] (:user (:session req)))

(defn handle-response [swirl-id response-button custom-response author]
  (let [summary (if (clojure.string/blank? custom-response) response-button custom-response)]
    (repo/create-response swirl-id summary author)
    (redirect (str "/swirls/" swirl-id))))

(defn handle-comment [swirl-id comment author]
  (do
    (repo/create-comment swirl-id comment author)
    (redirect (str "/swirls/" swirl-id))))

(defroutes swirl-routes
           (GET "/swirls/create" [_] (create-swirl-page "" "" "" nil))
           (POST "/swirls/create" [who subject review :as req] (handle-create-swirl who subject review (session-from req)))
           (GET "/swirls/:id" [id :as req] (view-swirl-page (Integer/parseInt id) (session-from req)))
           (POST "/swirls/:id/respond" [id responseButton response-summary :as req] (handle-response (Integer/parseInt id) responseButton response-summary (session-from req)))
           (POST "/swirls/:id/comment" [id comment :as req] (handle-comment (Integer/parseInt id) comment (session-from req)))
           (GET "/swirls/by/:authorName" [authorName] (view-swirls-by authorName)))