(ns yswrl.swirls.swirl-routes
  (:require [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.user.networking :as network]
            [yswrl.swirls.suggestion-job :refer [send-unsent-suggestions]]
            [yswrl.swirls.comment-notifier :refer [send-comment-notification-emails]]
            [yswrl.auth.auth-repo :as user-repo]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [status redirect response not-found]]
            [clojure.tools.logging :as log])
  (:import (java.util UUID)))

(defn edit-swirl-page [author swirl-id]
  (if-let [swirl (repo/get-swirl swirl-id)]
    (if (not= (swirl :author_id) (author :id))
      nil
      (let [contacts (network/get-relations (author :id) :knows)]
        (layout/render "swirls/create.html" {:id       swirl-id
                                             :subject  (swirl :title)
                                             :review   (swirl :review)
                                             :contacts contacts})))))

(defn view-inbox [count current-user]
  (let [userId (:id current-user)
        swirls (repo/get-swirls-for userId 20 count)]
    (println userId)
    (layout/render "swirls/firehose.html" {:pageTitle (str "Inbox") :swirls swirls :countFrom (str count) :countTo (+ count 20)})))


(def not-nil? (complement nil?))

(defn logister-info [is-logged-in suggestion-code]
  (try
    (if (and (not is-logged-in) (not-nil? suggestion-code))
      (if-let [sug (repo/get-suggestion (UUID/fromString suggestion-code))]
        (if (nil? (sug :recipient_id))
          (let [email (sug :recipient_email)
                username (user-repo/suggest-username (.substring email 0 (max 0 (.indexOf email "@"))))]
            {:register-username username :register-email email})
          (if-let [user (user-repo/get-user-by-id (sug :recipient_id))]
            { :login-username (user :username)}))))
    (catch Exception e (log/warn "Error while getting logister info" suggestion-code e))))

(defn view-swirl-page [id suggestion-code current-user]
  (if-let [swirl (repo/get-swirl id)]
    (let [is-logged-in (not-nil? current-user)
          is-author (and is-logged-in (= (swirl :author_id) (current-user :id)))
          logister-info (logister-info is-logged-in suggestion-code)
          responses (repo/get-swirl-responses (:id swirl))
          comments (repo/get-swirl-comments (:id swirl))
          can-respond (and (not is-author) is-logged-in (not-any? (fn [c] (= (:id current-user) (:responder c))) responses))
          can-edit is-author]
      (layout/render "swirls/view.html" {:swirl swirl :is-author is-author :responses responses :comments comments :can-respond can-respond :can-edit can-edit :logister-info logister-info}))))

(defn view-swirls-by [authorName]
  (if-let [author (user-repo/get-user authorName)]
    (if (= authorName (author :username))
      (let [swirls (repo/get-swirls-authored-by (:id author))]
        (layout/render "swirls/list.html" {:pageTitle (str "Reviews by " (author :username)) :author author :swirls swirls}))
      (redirect (str "/swirls/by/" (java.net.URLEncoder/encode (author :username) "UTF-8"))))))

(defn view-all-swirls [count]
  (if-let [swirls (repo/get-recent-swirls 20 count)]
    (layout/render "swirls/firehose.html" {:pageTitle (str "Firehose") :swirls swirls :countFrom (str count) :countTo (+ count 20)})))

(defn session-from [req] (:user (:session req)))

(defn handle-response [swirl-id response-button custom-response author]
  (let [summary (if (clojure.string/blank? custom-response) response-button custom-response)]
    (repo/create-response swirl-id summary author)
    (redirect (yswrl.links/swirl swirl-id))))


(defn handle-comment [swirl-id comment-content author]
  (let [swirl (repo/get-swirl swirl-id)
        comment (repo/create-comment swirl-id comment-content author)]
    (send-comment-notification-emails comment)
    (if (not= (swirl :author_id) (author :id))
      (do (network/store (swirl :author_id) :knows (author :id))
          (network/store (author :id) :knows (swirl :author_id))))
    (redirect (str "/swirls/" swirl-id))))


(defn publish-swirl [author id who emails subject review]
  (let [emails (map clojure.string/trim (clojure.string/split emails #"[,;]"))
        namesOrEmails (filter (complement clojure.string/blank?) (distinct (concat (or who []) emails)))]
    (if (repo/publish-swirl id (author :id) subject review namesOrEmails)
      (do
        (send-unsent-suggestions)
        (redirect (yswrl.links/swirl id)))
      nil)))

(defroutes swirl-routes
           (GET "/swirls/:id{[0-9]+}/edit" [id :as req] (edit-swirl-page (session-from req) (Integer/parseInt id)))
           (POST "/swirls/:id{[0-9]+}/edit" [id who emails subject review :as req] (publish-swirl (session-from req) (Integer/parseInt id) who emails subject review))
           (GET "/swirls" [] (view-all-swirls 0))
           (GET "/swirls/:id{[0-9]+}" [id code :as req] (view-swirl-page (Integer/parseInt id) code (session-from req)))
           (POST "/swirls/:id{[0-9]+}/respond" [id responseButton response-summary :as req] (handle-response (Integer/parseInt id) responseButton response-summary (session-from req)))
           (POST "/swirls/:id{[0-9]+}/comment" [id comment :as req] (handle-comment (Integer/parseInt id) comment (session-from req)))
           (GET "/swirls/from/:count{[0-9]+}" [count] (view-all-swirls (Long/parseLong count)))
           (GET "/swirls/by/:authorName" [authorName] (view-swirls-by authorName))
           (GET "/swirls/inbox" [:as req] (view-inbox 0 (session-from req)))
           (GET "/swirls/inbox/:count{[0-9]+}" [:as req] (view-inbox count (session-from req))))
