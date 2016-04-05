(ns yswrl.swirls.swirl-routes
  (:require [yswrl.layout :as layout]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.user.networking :as network]
            [yswrl.user.user-selector :as user-selector]
            [yswrl.swirls.suggestion-job :refer [send-unsent-suggestions]]
            [yswrl.auth.auth-repo :as user-repo]
            [compojure.core :refer [defroutes GET POST]]
            [yswrl.links :as links]
            [ring.util.response :refer [status redirect response not-found]]
            [clojure.tools.logging :as log]
            [yswrl.auth.guard :as guard]
            [yswrl.swirls.types :refer [type-of] :as swirl-types]
            [yswrl.swirls.lookups :as lookups]
            [yswrl.user.notifications :as notifications]
            [yswrl.swirls.swirl-links :as link-types]
            [yswrl.utils :as utils]
            [clojure.string :refer [join lower-case]]
            [yswrl.groups.groups-repo :as group-repo]
            [yswrl.swirls.types :as types])
  (:import (java.util UUID)))

(defn seen-responses [type]
  ["Later" (get-in types/types [type :words :watching]) "Loved it", "Not bad", "Not for me"])


(defn should-notify-users-of-response [response]
  (not (some #{(lower-case response)} #{"dismissed" "later"})))
(def responses-to-hide-on-view-page #{"dismissed"})


(defn edit-swirl-page [author swirl-id group-id is-private? origin-swirl-id & {:keys [edit? wishlist]
                                                                               :or   {edit?    false
                                                                                      wishlist false}}]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-edit swirl-id (author :id))]
    (let [wishlist (boolean wishlist)
          already-added (set (repo/get-suggestion-usernames swirl-id))
          contacts (network/get-relations (author :id) :knows)
          origin-swirl (lookups/get-swirl origin-swirl-id)
          origin-swirl-author (if origin-swirl (user-repo/get-user-by-id (origin-swirl :author_id)))
          all-users (user-repo/get-all-users)
          suggested-users (take 5 (filter #(and (not (contains? already-added %))
                                                (not (= (:author_id origin-swirl) (:user-id %)))) contacts))
          all-not-added (filter #(and (not (contains? already-added %))
                                      (not (= (:author_id origin-swirl) (:user-id %)))) all-users)
          all-groups (group-repo/get-groups-for (author :id))
          already-selected-groups (group-repo/get-groups-linked-to-swirl swirl-id)
          selected-groups (concat already-selected-groups (filter #(= (.toString (% :id)) group-id) all-groups))
          groups-model (map (fn [g] {:group g :selected (utils/in? selected-groups g)}) all-groups)]
      (layout/render "swirls/edit.html" {:id                  swirl-id
                                         :subject             (swirl :title)
                                         :review              (swirl :review)
                                         :type                (type-of swirl)
                                         :all-types           (keys swirl-types/types)

                                         :swirl               swirl
                                         :edit?               edit?
                                         :contacts            suggested-users
                                         :already-suggested   already-added
                                         :unrelated           all-not-added
                                         :origin-swirl-id     origin-swirl-id
                                         :wishlist            wishlist
                                         :groups-model        groups-model
                                         :origin-swirl-author origin-swirl-author
                                         :is_private          (or is-private? (:is_private swirl))}))))
(defn delete-swirl-page [author swirl-id]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-edit swirl-id (author :id))]
    (layout/render "swirls/delete.html" {:swirl swirl})))

(defn view-inbox [count current-user]
  (let [swirls-per-page 20
        swirls (lookups/get-swirls-awaiting-response current-user 200 count)
        responses (lookups/get-response-count-for-user (:id current-user))
        has-inbox-items? (not (empty? swirls))
        has-responses? (not (empty? responses))
        nothing-to-show? (and (not has-inbox-items?) (not has-responses?))]
    (layout/render "swirls/inbox.html" {:title             "Swirl Inbox"
                                        :swirls            (take swirls-per-page swirls)
                                        :responses         responses
                                        :more-swirls       (join "," (map :id (nthrest swirls swirls-per-page)))
                                        :has-inbox-items?  has-inbox-items? :has-responses? has-responses? :nothing-to-show? nothing-to-show?
                                        :paging-url-prefix "/swirls/inbox?from="
                                        :swirls-per-page   swirls-per-page
                                        :countFrom         (str count)
                                        :countTo           (+ count swirls-per-page)})))

(defn view-firehose [from user]
  (let [swirls-per-page 20
        swirls (lookups/get-all-swirls 200 from user)]
    (layout/render "swirls/list.html" {:title             "The Firehose - all public swirls"
                                       :pageTitle         "Public Swrls"
                                       :swirls            (take swirls-per-page swirls)
                                       :more-swirls       (join "," (map :id (nthrest swirls swirls-per-page)))
                                       :paging-url-prefix "/swirls?from="
                                       :swirls-per-page   swirls-per-page
                                       :countFrom         (str from)
                                       :countTo           (+ from swirls-per-page)})))

(defn search [query user]
  (let [swirls (if (clojure.string/blank? query) [] (lookups/search-for-swirls 100 0 user query))]
    (layout/render "swirls/search-swirls.html" {:title  "Search results"
                                                :swirls swirls
                                                :query  query})))


(defn view-inbox-by-response [count current-user submitted-response]
  (let [swirls (lookups/get-swirls-by-response current-user 2000 count submitted-response)
        is-later (.equalsIgnoreCase "later" submitted-response)
        title (if is-later "Saved for later" submitted-response)]
    (layout/render "swirls/list-by-response.html" {
                                                   :title              title
                                                   :submitted-response submitted-response
                                                   :is-later           is-later
                                                   :pageTitle          title
                                                   :swirls             swirls
                                                   :responses          (lookups/get-response-count-for-user (:id current-user))
                                                   :countFrom          (str count)
                                                   :countTo            (+ count 20)})))

(def not-nil? (complement nil?))

(defn get-html-of-comments-since [user swirl-id comment-id-to-start-from]
  (if (lookups/get-swirl-if-allowed-to-view swirl-id user)
    (let [comments (repo/get-swirl-comments swirl-id comment-id-to-start-from)]
      (response {:maxId (reduce max 0 (map #(:id %) comments))
                 :count (count comments)
                 :html  (layout/render-string "components/comment-list.html"
                                              {:thecomments comments})}))))

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


(defn website-link-if-appropriate [swirl links]
  (:code (first (filter #(= (% :type_code) "W") links))))

(defn response-summary-as-html [response-summary]
  (str "<i class=\"fa fa-fw title-color "
       (get layout/response-icons (clojure.string/lower-case response-summary) "fa-star")
       "\"></i> "
       response-summary))

(defn view-swirl-page [id suggestion-code current-user]

  (if-let [swirl (lookups/get-swirl-if-allowed-to-view id current-user)]
    (let [is-logged-in (not-nil? current-user)
          is-author (and is-logged-in (= (swirl :author_id) (current-user :id)))
          logister-info (logister-info is-logged-in suggestion-code)
          responses (map #(assoc % :html_content (response-summary-as-html (:summary %))) (repo/get-swirl-responses (:id swirl) responses-to-hide-on-view-page))
          comments (repo/get-swirl-comments (:id swirl))
          non-responders (repo/get-non-responders (:id swirl))
          can-respond is-logged-in
          response-of-current-user (if is-logged-in (first (filter #(= (:id current-user) (:responder %)) responses)) nil)
          type (type-of swirl)
          title (str "You should " (get-in type [:words :watch]) " " (swirl :title))
          swirl-links (repo/get-links id)
          external-website-link (website-link-if-appropriate swirl swirl-links)
          max-comment-id (reduce max 0 (map :id comments))
          seen-response-options (if can-respond
                                  (distinct (let [seen-responses (seen-responses (:type swirl))]
                                              (concat seen-responses
                                                      (sort (repo/get-recent-responses-by-user-and-type (current-user :id) (swirl :type) seen-responses))
                                                      (if (not (nil? response-of-current-user)) [(response-of-current-user :summary)] []))))
                                  [])
          can-edit is-author]
      (notifications/mark-as-seen id current-user)
      (layout/render "swirls/view-page.html"
                     {:title         title
                      :logister-info logister-info
                      :return-url    (links/swirl id)
                      :model         {
                                      :title                    title
                                      :swirl                    swirl
                                      :swirl-links              swirl-links
                                      :external-website-link    external-website-link
                                      :type                     type
                                      :is-author                is-author
                                      :responses                responses
                                      :comments                 (sort-by :date_responded (concat comments responses))
                                      :max-comment-id           max-comment-id
                                      :can-respond              can-respond
                                      :can-edit                 can-edit
                                      :non-responders           non-responders
                                      :response-of-current-user response-of-current-user
                                      :seen-response-options    seen-response-options}}))))

(defn view-profile [author-username, current-user]
  (if-let [author (user-repo/get-user author-username)]
    (if (= author-username (author :username))
      (let [swirls (lookups/get-swirls-authored-by (:id author) current-user)
            is-current-user (and (not-nil? current-user) (= (current-user :username) (author :username)))
            notifications (notifications/get-notification-view-model author)
            responses (lookups/get-response-count-for-user (:id author))
            has-responses? (not (empty? responses))]
        (layout/render "users/public-profile.html" {:title         (str "Reviews by " (author :username)) :author author :swirls swirls :is-current-user is-current-user
                                                    :notifications notifications :has-responses? has-responses? :responses responses}))
      (redirect (links/user (author :username))))))

(defn session-from [req] (:user (:session req)))

(defn handle-response [swirl-id response-button custom-response responder]
  (if (lookups/get-swirl-if-allowed-to-view swirl-id responder)
    (let [summary (if (clojure.string/blank? custom-response) response-button custom-response)
          swrl-list-state (get {"Later"     "wishlist"
                                "Reading"   "consuming"
                                "Playing"   "consuming"
                                "Listening" "consuming"
                                "Watching"  "consuming"
                                "Looking"   "consuming"
                                "Using"     "consuming"}
                               summary "done")
          swirl-response (repo/respond-to-swirl swirl-id summary responder)]
      (if (= "Dismissed" summary)
        (repo/remove-from-watchlist swirl-id responder)
        (repo/add-swirl-to-wishlist swirl-id swrl-list-state responder))
      (if (should-notify-users-of-response summary)
        (notifications/add-to-watchers-of-swirl notifications/new-response swirl-id (swirl-response :id) (responder :id) summary))
      (redirect (yswrl.links/swirl swirl-id)))))

(defn handle-comment [swirl-id comment-content commentor]
  (log/info "Handling comment: " swirl-id " : " comment-content " : " commentor)
  (let [swirl (lookups/get-swirl-if-allowed-to-view swirl-id commentor)
        comment (repo/create-comment swirl-id comment-content commentor)]
    (notifications/add-to-watchers-of-swirl notifications/new-comment swirl-id (comment :id) (commentor :id) nil)
    (if (not= (swirl :author_id) (commentor :id))
      (do (network/store (swirl :author_id) :knows (commentor :id))
          (network/store (commentor :id) :knows (swirl :author_id))))
    (redirect (yswrl.links/swirl swirl-id (comment :id)))))


(defn publish-swirl
  ([author id usernames-and-emails-to-notify subject review origin-swirl-id group-ids private? type image-url wishlist]
   (if (repo/publish-swirl id (author :id) subject review usernames-and-emails-to-notify private? type image-url)
     (do
       (group-repo/set-swirl-links id (author :id) group-ids)
       (doseq [group-id group-ids]
         (let [members (group-repo/get-group-members group-id)
               members-sans-author (filter #(not (= (% :id) (author :id))) members)]
           (repo/add-suggestions id (author :id) (map :username members-sans-author))))
       (if wishlist
         (handle-response id nil "Later" author))

       (if (not-nil? origin-swirl-id)
         (do
           (repo/add-link id (link-types/swirl-progenitor :code) origin-swirl-id)
           (repo/add-link origin-swirl-id (link-types/swirl-response :code) id)
           (handle-comment origin-swirl-id (str "You should also " (get-in (yswrl.swirls.types/type-of (lookups/get-swirl id))
                                                                           [:words :watch])
                                                ": <a href=\"" (yswrl.links/swirl id) "\">"
                                                subject "</a>")
                           author))
         (redirect (yswrl.links/swirl id))))
     nil)))


(defn delete-swirl [current-user swirl-id]
  (if-let [swirl (lookups/get-swirl-if-allowed-to-edit swirl-id (current-user :id))]
    (do
      (repo/delete-swirl (swirl :id) (current-user :id))
      (redirect (links/user (current-user :username))))))

(defn groups-page []
  (layout/render "swirls/groups.html" {:title "Swirl groups"}))


(defn post-response-route [url-prefix]
  (POST (str url-prefix "/:id{[0-9]+}/respond") [id responseButton response-summary :as req] (guard/requires-login #(handle-response (Long/parseLong id) responseButton response-summary (session-from req)))))


(defn post-comment-route [url-prefix]
  (POST (str url-prefix "/:id{[0-9]+}/comment") [id comment :as req] (guard/requires-login #(handle-comment (Long/parseLong id) comment (session-from req)))))

(defn vectorise [value-or-vector]
  (if (nil? value-or-vector) []
                             (if (vector? value-or-vector) value-or-vector [value-or-vector])))

(defn numberise [strings]
  (map #(Long/parseLong % 10) strings))

(defn get-swirls-by-id []
  (GET "/" [swirl-list :as req]
    (let [ids (map #(Long/parseLong %) (clojure.string/split swirl-list #","))
          swirls (lookups/get-swirls-by-id ids (session-from req))]
      (layout/render "swirls/swirl-list-for-rest-api.html"
                     {:swirls swirls}))))

(defn search-for-swirls []
  (GET "/search" [query :as req]
    (let [swirls (lookups/search-for-swirls 100 0 (session-from req) query)]
      (layout/render "swirls/swirl-list-for-rest-api.html"
                     {:swirls swirls}))))


(defroutes swirl-routes
           (GET "/swirls/:id{[0-9]+}/edit" [id origin-swirl-id group-id is-private edit wishlist :as req]
             (guard/requires-login #(edit-swirl-page (session-from req) (Long/parseLong id) group-id (= "true" is-private) (if (clojure.string/blank? origin-swirl-id)
                                                                                                                             nil
                                                                                                                             (Long/parseLong origin-swirl-id))
                                                     :edit? edit
                                                     :wishlist wishlist)))
           (POST "/swirls/:id{[0-9]+}/edit" [id origin-swirl-id who emails subject review groups private swirl-type image-url wishlist :as req]
             (guard/requires-login #(publish-swirl
                                     (session-from req)
                                     (Long/parseLong id)
                                     (user-selector/usernames-and-emails-from-request who emails)
                                     subject
                                     review
                                     (if (clojure.string/blank? origin-swirl-id) nil (Long/parseLong origin-swirl-id))
                                     (numberise (vectorise groups))
                                     (if private
                                       true
                                       false)
                                     swirl-type
                                     image-url
                                     (= wishlist "true"))))

           (GET "/swirls/:id{[0-9]+}/delete" [id :as req] (guard/requires-login #(delete-swirl-page (session-from req) (Long/parseLong id))))
           (POST "/swirls/:id{[0-9]+}/delete" [id :as req] (guard/requires-login #(delete-swirl (session-from req) (Long/parseLong id))))

           (GET "/swirls/:id{[0-9]+}" [id code :as req] (view-swirl-page (Long/parseLong id) code (session-from req)))

           (post-response-route "/swirls")
           (post-comment-route "/swirls")

           (GET "/swirls" [from :as req] (view-firehose (Long/parseLong (if (clojure.string/blank? from) "0" from)) (session-from req)))

           (GET "/search" [query :as req] (search query (session-from req)))

           (GET "/swirls/groups" [] (groups-page))

           (GET "/profile/:authorName" [authorName :as req] (view-profile authorName (session-from req)))
           (GET "/swirls/inbox" [:as req] (guard/requires-login #(view-inbox 0 (session-from req))))
           (GET "/swirls/inbox/:response" [response :as req] (guard/requires-login #(view-inbox-by-response 0 (session-from req) response))))
