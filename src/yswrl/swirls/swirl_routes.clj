(ns yswrl.swirls.swirl-routes
  (:require [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.user.networking :as network]
            [yswrl.swirls.suggestion-job :refer [send-unsent-suggestions]]
            [yswrl.swirls.comment-notifier :refer [send-comment-notification-emails]]
            [yswrl.swirls.response-notifier :refer [send-response-notification-emails]]
            [yswrl.auth.auth-repo :as user-repo]
            [compojure.core :refer [defroutes GET POST]]
            [yswrl.links :as links]
            [ring.util.response :refer [status redirect response not-found]]
            [clojure.tools.logging :as log]
            [yswrl.auth.guard :as guard]
            [yswrl.swirls.types :refer [type-of]]
            [yswrl.swirls.lookups :as lookups]
            [yswrl.user.notifications :as notifications])
  (:import (java.util UUID)))

(def seen-responses ["Loved it", "Not bad", "Meh"])
(def not-seen-responses ["Later", "Not for me"])

(defn edit-swirl-page [author swirl-id]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-edit swirl-id (author :id))]
    (let [already-suggested (set (repo/get-suggestion-usernames swirl-id))
          contacts (network/get-relations (author :id) :knows)
          not-added (filter #(not (contains? already-suggested %)) contacts)]
      (layout/render "swirls/edit.html" {:id                swirl-id
                                         :subject           (swirl :title)
                                         :review            (swirl :review)
                                         :type              (type-of swirl)
                                         :contacts          not-added
                                         :already-suggested already-suggested}))))
(defn delete-swirl-page [author swirl-id]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-edit swirl-id (author :id))]
    (layout/render "swirls/delete.html" {:swirl swirl})))

(defn view-inbox [count current-user]
  (let [swirls (lookups/get-swirls-awaiting-response (:id current-user) 2000 count)
        responses (repo/get-response-count-for-user (:id current-user))]
    (layout/render "swirls/list-with-profile.html" {:title "Swirl Inbox" :pageTitle "Inbox" :swirls swirls :countFrom (str count) :countTo (+ count 20) :response-counts responses})))

(defn view-inbox-by-response [count current-user submitted-response]
  (println "Submitted response:" submitted-response)
  (let [swirls (lookups/get-swirls-by-response (:id current-user) 2000 count submitted-response)
        responses (repo/get-response-count-for-user (:id current-user))]
    (layout/render "swirls/list-with-profile.html" {:title submitted-response :pageTitle submitted-response :swirls swirls :countFrom (str count) :countTo (+ count 20) :response-counts responses})))

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
            {:login-username (user :username)}))))
    (catch Exception e (log/warn "Error while getting logister info" suggestion-code e))))

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

(defn view-swirl-page [id suggestion-code current-user]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-view id (get current-user :id nil))]
    (let [is-logged-in (not-nil? current-user)
          is-author (and is-logged-in (= (swirl :author_id) (current-user :id)))
          logister-info (logister-info is-logged-in suggestion-code)
          responses (repo/get-swirl-responses (:id swirl))
          comments (repo/get-swirl-comments (:id swirl))
          non-responders (repo/get-non-responders (:id swirl))
          can-respond (and (not is-author) is-logged-in)
          response-of-current-user (if is-logged-in (first (filter #(= (:id current-user) (:responder %)) responses)) nil)
          type (type-of swirl)
          title (str "You should " (get-in type [:words :watch]) " " (swirl :title))
          swirl-links (repo/get-links id)
          seen-response-options (if can-respond
                                  (distinct (concat seen-responses
                                                    (sort (repo/get-recent-responses-by-user-and-type (current-user :id) (swirl :type) (concat seen-responses not-seen-responses)))
                                                    (if (and (not (nil? response-of-current-user)) (not (in? not-seen-responses (response-of-current-user :summary)))) [(response-of-current-user :summary)] [])))
                                  [])

          can-edit is-author]
      (notifications/mark-as-seen id current-user)
      (layout/render "swirls/view.html" {
                                         :title                    title :swirl swirl :swirl-links swirl-links :type type :is-author is-author
                                         :responses                responses :comments comments :can-respond can-respond :can-edit can-edit
                                         :logister-info            logister-info :non-responders non-responders
                                         :response-of-current-user response-of-current-user :seen-response-options seen-response-options :not-seen-response-options not-seen-responses}))))

(defn view-swirls-by [authorName]
  (if-let [author (user-repo/get-user authorName)]
    (if (= authorName (author :username))
      (let [swirls (lookups/get-swirls-authored-by (:id author))]
        (layout/render "swirls/list.html" {:pageTitle (str "Reviews by " (author :username)) :author author :swirls swirls}))
      (redirect (links/user (author :username))))))

(defn view-all-swirls [count]
  (if-let [swirls (lookups/get-recent-swirls 20 count)]
    (layout/render "swirls/list.html" {:pageTitle "Firehose" :swirls swirls :countFrom (str count) :countTo (+ count 20)})))

(defn session-from [req] (:user (:session req)))

(defn handle-response [swirl-id response-button custom-response author]
  (if (lookups/get-swirl-if-allowed-to-view swirl-id (author :id))
    (let [summary (if (clojure.string/blank? custom-response) response-button custom-response)
          swirl-response (repo/respond-to-swirl swirl-id summary author)]
      (send-response-notification-emails swirl-response author)
      (redirect (yswrl.links/swirl swirl-id)))))


(defn handle-comment [swirl-id comment-content author]
  (let [swirl (lookups/get-swirl-if-allowed-to-view swirl-id (author :id))
        comment (repo/create-comment swirl-id comment-content author)]
    (send-comment-notification-emails comment)
    (if (not= (swirl :author_id) (author :id))
      (do (network/store (swirl :author_id) :knows (author :id))
          (network/store (author :id) :knows (swirl :author_id))))
    (redirect (str "/swirls/" swirl-id))))


(defn publish-swirl [author id usernames-and-emails-to-notify subject review]
  (if (repo/publish-swirl id (author :id) subject review usernames-and-emails-to-notify)
    (do
      (send-unsent-suggestions)
      (redirect (yswrl.links/swirl id)))
    nil))

(defn usernames-and-emails-from-request [checkboxes-raw textbox-raw]
  (let [textbox (if (clojure.string/blank? textbox-raw)
                  []
                  (map #(.trim %) (clojure.string/split textbox-raw #"[,;]")))
        checkboxes (if (vector? checkboxes-raw)
                     checkboxes-raw
                     (if (clojure.string/blank? checkboxes-raw)
                       []
                       [(.trim checkboxes-raw)]))
        ]
    (distinct (concat checkboxes textbox))))

(defn delete-swirl [current-user swirl-id]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-edit swirl-id (current-user :id))]
    (do
      (repo/delete-swirl (swirl :id) (current-user :id))
      (redirect (links/user (current-user :username))))))

(defroutes swirl-routes
           (GET "/swirls/:id{[0-9]+}/edit" [id :as req] (guard/requires-login #(edit-swirl-page (session-from req) (Long/parseLong id))))
           (POST "/swirls/:id{[0-9]+}/edit" [id who emails subject review :as req] (guard/requires-login #(publish-swirl (session-from req) (Long/parseLong id) (usernames-and-emails-from-request who emails) subject review)))

           (GET "/swirls/:id{[0-9]+}/delete" [id :as req] (guard/requires-login #(delete-swirl-page (session-from req) (Long/parseLong id))))
           (POST "/swirls/:id{[0-9]+}/delete" [id :as req] (guard/requires-login #(delete-swirl (session-from req) (Long/parseLong id))))

           (GET "/swirls" [] (view-all-swirls 0))
           (GET "/swirls/:id{[0-9]+}" [id code :as req] (view-swirl-page (Integer/parseInt id) code (session-from req)))
           (POST "/swirls/:id{[0-9]+}/respond" [id responseButton response-summary :as req] (guard/requires-login #(handle-response (Integer/parseInt id) responseButton response-summary (session-from req))))
           (POST "/swirls/:id{[0-9]+}/comment" [id comment :as req] (guard/requires-login #(handle-comment (Integer/parseInt id) comment (session-from req))))
           (GET "/swirls/from/:count{[0-9]+}" [count] (view-all-swirls (Long/parseLong count)))
           (GET "/swirls/by/:authorName" [authorName] (view-swirls-by authorName))
           (GET "/swirls/inbox" [:as req] (guard/requires-login #(view-inbox 0 (session-from req))))
           (GET "/swirls/inbox/:response" [response :as req] (guard/requires-login #(view-inbox-by-response 0 (session-from req) response))))
