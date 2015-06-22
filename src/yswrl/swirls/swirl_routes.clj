(ns yswrl.swirls.swirl-routes
  (:require [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.user.networking :as network]
            [yswrl.swirls.suggestion-job :refer [send-unsent-suggestions]]
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

(def seen-responses ["Loved it", "Not bad", "Meh", "Later", "Not for me"])

(defn edit-swirl-page [author swirl-id]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-edit swirl-id (author :id))]
    (let [already-suggested (set (repo/get-suggestion-usernames swirl-id))
          contacts (network/get-relations (author :id) :knows)
          not-added (filter #(not (contains? already-suggested %)) contacts)
          unrelated (network/get-unrelated-users (author :id) 100 0)]
      (layout/render "swirls/edit.html" {:id                swirl-id
                                         :subject           (swirl :title)
                                         :review            (swirl :review)
                                         :type              (type-of swirl)
                                         :contacts          not-added
                                         :already-suggested already-suggested
                                         :unrelated         unrelated}))))
(defn delete-swirl-page [author swirl-id]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-edit swirl-id (author :id))]
    (layout/render "swirls/delete.html" {:swirl swirl})))

(defn view-inbox [count current-user]
  (let [swirls (lookups/get-swirls-awaiting-response (:id current-user) 2000 count)]
    (layout/render "swirls/list.html" {:title "Swirl Inbox" :pageTitle "Inbox" :swirls swirls :countFrom (str count) :countTo (+ count 20)})))

(defn view-firehose [count]
  (let [swirls (lookups/get-all-swirls 20 count)]
    (layout/render "swirls/list.html" {:title "Firehose" :pageTitle "Firehose" :swirls swirls
                                       :paging-url-prefix "/swirls?from="
                                       :countFrom (str count) :countTo (+ count 20)})))


(defn view-inbox-by-response [count current-user submitted-response]
  (let [swirls (lookups/get-swirls-by-response (:id current-user) 2000 count submitted-response)]
    (layout/render "swirls/list.html" {:title submitted-response :pageTitle submitted-response :swirls swirls :countFrom (str count) :countTo (+ count 20)})))

(def not-nil? (complement nil?))

(defn get-html-of-comments-since [user swirl-id comment-id-to-start-from]
  (if (lookups/get-swirl-if-allowed-to-view swirl-id (user :id))
    (let [comments (repo/get-swirl-comments swirl-id comment-id-to-start-from)]
      (response {:maxId (reduce max 0 (map #(:id %) comments))
                 :count (count comments)
                 :html  (layout/render-string "components/comment-list.html"
                                              {:comments comments})}))))

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
          non-responders (if is-author (repo/get-non-responders (:id swirl)))
          can-respond (and (not is-author) is-logged-in)
          response-of-current-user (if is-logged-in (first (filter #(= (:id current-user) (:responder %)) responses)) nil)
          type (type-of swirl)
          title (str "You should " (get-in type [:words :watch]) " " (swirl :title))
          swirl-links (repo/get-links id)
          max-comment-id (reduce max 0 (map #(:id %) comments))
          seen-response-options (if can-respond
                                  (distinct (concat seen-responses
                                                    (sort (repo/get-recent-responses-by-user-and-type (current-user :id) (swirl :type) seen-responses))
                                                    (if (not (nil? response-of-current-user)) [(response-of-current-user :summary)] [])))
                                  [])

          can-edit is-author]
      (notifications/mark-as-seen id current-user)
      (layout/render "swirls/view.html" {
                                         :title                    title :swirl swirl :swirl-links swirl-links :type type :is-author is-author
                                         :responses                responses :comments comments :max-comment-id max-comment-id :can-respond can-respond :can-edit can-edit
                                         :logister-info            logister-info :non-responders non-responders
                                         :response-of-current-user response-of-current-user :seen-response-options seen-response-options}))))

(defn view-swirls-by [authorName]
  (if-let [author (user-repo/get-user authorName)]
    (if (= authorName (author :username))
      (let [swirls (lookups/get-swirls-authored-by (:id author))]
        (layout/render "swirls/list.html" {:pageTitle (str "Reviews by " (author :username)) :author author :swirls swirls}))
      (redirect (links/user (author :username))))))

(defn session-from [req] (:user (:session req)))

(defn handle-response [swirl-id response-button custom-response responder]
  (if (lookups/get-swirl-if-allowed-to-view swirl-id (responder :id))
    (let [summary (if (clojure.string/blank? custom-response) response-button custom-response)
          swirl-response (repo/respond-to-swirl swirl-id summary responder)]
      (notifications/add-to-watchers-of-swirl notifications/new-response swirl-id (swirl-response :id) (responder :id) summary)
      (redirect (yswrl.links/swirl swirl-id)))))

(defn handle-comment [swirl-id comment-content commentor]
  (let [swirl (lookups/get-swirl-if-allowed-to-view swirl-id (commentor :id))
        comment (repo/create-comment swirl-id comment-content commentor)]
    (notifications/add-to-watchers-of-swirl notifications/new-comment swirl-id (comment :id) (commentor :id) nil)
    (if (not= (swirl :author_id) (commentor :id))
      (do (network/store (swirl :author_id) :knows (commentor :id))
          (network/store (commentor :id) :knows (swirl :author_id))))
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


(defn post-response-route [url-prefix]
  (POST (str url-prefix "/:id{[0-9]+}/respond") [id responseButton response-summary :as req] (guard/requires-login #(handle-response (Long/parseLong id) responseButton response-summary (session-from req)))))

(defn post-comment-route [url-prefix]
  (POST (str url-prefix "/:id{[0-9]+}/comment") [id comment :as req] (guard/requires-login #(handle-comment (Long/parseLong id) comment (session-from req)))))


(defroutes swirl-routes
           (GET "/swirls/:id{[0-9]+}/edit" [id :as req] (guard/requires-login #(edit-swirl-page (session-from req) (Long/parseLong id))))
           (POST "/swirls/:id{[0-9]+}/edit" [id who emails subject review :as req] (guard/requires-login #(publish-swirl (session-from req) (Long/parseLong id) (usernames-and-emails-from-request who emails) subject review)))

           (GET "/swirls/:id{[0-9]+}/delete" [id :as req] (guard/requires-login #(delete-swirl-page (session-from req) (Long/parseLong id))))
           (POST "/swirls/:id{[0-9]+}/delete" [id :as req] (guard/requires-login #(delete-swirl (session-from req) (Long/parseLong id))))

           (GET "/swirls/:id{[0-9]+}" [id code :as req] (view-swirl-page (Long/parseLong id) code (session-from req)))

           (post-response-route "/swirls")
           (post-comment-route "/swirls")

           (GET "/swirls" [from] (view-firehose (Long/parseLong (if (clojure.string/blank? from) "0" from))))

           (GET "/swirls/by/:authorName" [authorName] (view-swirls-by authorName))
           (GET "/swirls/inbox" [:as req] (guard/requires-login #(view-inbox 0 (session-from req))))
           (GET "/swirls/inbox/:response" [response :as req] (guard/requires-login #(view-inbox-by-response 0 (session-from req) response))))
