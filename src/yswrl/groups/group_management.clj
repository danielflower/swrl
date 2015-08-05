(ns yswrl.groups.group-management
  (:require
    [yswrl.layout :as layout]
    [compojure.core :refer [defroutes GET POST]]
    [ring.util.response :refer [status redirect response not-found]]
    [yswrl.user.user-selector :as user-selector]
    [yswrl.auth.guard :as guard]
    [yswrl.links :as links]
    [yswrl.user.networking :as network]
    [yswrl.groups.groups-repo :as repo]
    [yswrl.auth.auth-repo :as auth]
    [yswrl.user.notifications :as notifications]
    [yswrl.utils :refer :all]
    [yswrl.db :as db]))
(use 'korma.core)
(use 'korma.db)



(defn view-group-page [group-id user]
  (if-let [group (repo/get-group-if-allowed group-id (user :id))]
    (do
      (notifications/mark-subject-as-seen group-id user)
      (layout/render "groups/view-group.html" {:title    (group :name)
                                               :group    group
                                               :security {:can-edit (= (group :created_by_id) (user :id))}
                                               :swirls   (repo/get-swirls-for-group group-id user)
                                               :members  (repo/get-group-members group-id)}))))


(defn create-group-page [user errors group-title group_description]
  (layout/render "groups/create-group.html" {:title              "Create a group"
                                             :suggested-contacts (network/get-relations (user :id) :knows)
                                             :other-contacts     (network/get-unrelated-users (user :id) 100 0)
                                             :errors             errors :group-title group-title :group-description group_description
                                             :post-url           "/create-group"
                                             :cancel-url         "/"}))

(defn edit-group-page [user group-id errors]
  (if-let [group (repo/get-group-if-allowed group-id (user :id))]
    (let [already-selected (repo/get-group-members group-id)]
      (layout/render "groups/create-group.html" {:title              "Edit group"
                                                 :group-name         (group :name)
                                                 :group-description  (group :description)
                                                 :already-selected   already-selected
                                                 :suggested-contacts (filter #(not (in? (map :username already-selected) (% :username))) (network/get-relations (user :id) :knows))
                                                 :other-contacts     (network/get-unrelated-users (user :id) 100 0)
                                                 :errors             errors
                                                 :post-url           (links/edit-group group-id)
                                                 :cancel-url         (links/group group-id)}))))


(defn set-group-membership [group-id added-by-id user-ids]
  (let [user-set (set user-ids)
        current (set (map :id (repo/get-group-members group-id)))
        to-add (clojure.set/difference user-set current)    ; items selected but not already in the current set
        to-delete (clojure.set/difference current user-set) ; any current ones which weren't passed in
        ]
    (doseq [user-id to-add]
      (notifications/add notifications/added-to-group user-id nil group-id added-by-id)
      (repo/add-group-member group-id user-id))
    (doseq [user-id to-delete]
      (delete db/group-members
              (where {:group_id group-id :user_id user-id})))
    ))

(defn save-group [creator group member-usernames-and-emails]
  (let [found-users (auth/get-users-by-username_or_email (distinct member-usernames-and-emails))]
    (set-group-membership (group :id) (creator :id) (map :id found-users))
    (redirect (links/group (group :id)))))

(defn create-group [creator group-name group-description member-usernames-and-emails]
  (transaction
    (let [group (repo/create-group (creator :id) group-name group-description)
          redirect (save-group creator group member-usernames-and-emails)]
      (repo/add-group-member (group :id) (creator :id))
      redirect)))

(defn update-group [creator group-id group-name group-description member-usernames-and-emails]
  (transaction
    (if-let [group (repo/get-group-if-allowed group-id (creator :id))]
      (do
        (repo/update-group group-id group-name group-description)
        (save-group creator group member-usernames-and-emails)))))

(defn join-group [group-id join-code user]
  (if-let [group (repo/get-group-by-code group-id join-code)]
    (let [not-already-member? (nil? (repo/get-group-if-allowed group-id (user :id)))]
      (if not-already-member?
        (repo/add-group-member group-id (user :id)))
      (redirect (links/group (group :id))))))

(defroutes group-routes

           (GET "/groups/:id{[0-9]+}" [id :as req] (guard/requires-login #(view-group-page (Long/parseLong id) (user-from-session req))))

           (GET "/create-group" [:as req] (guard/requires-login #(create-group-page (user-from-session req) nil nil nil)))
           (POST "/create-group" [group-name group-description who emails :as req] (guard/requires-login #(create-group (user-from-session req) group-name group-description (user-selector/usernames-and-emails-from-request who emails))))
           (GET "/groups/:id{[0-9]+}/join/:code{[0-9]+}" [id code :as req] (guard/requires-login #(join-group (Long/parseLong id) (Long/parseLong code) (user-from-session req))))

           (GET "/groups/:id{[0-9]+}/edit" [id :as req] (guard/requires-login #(edit-group-page (user-from-session req) (Long/parseLong id) nil)))
           (POST "/groups/:id{[0-9]+}/edit" [id group-name group-description who emails :as req] (guard/requires-login #(update-group (user-from-session req) (Long/parseLong id) group-name group-description (user-selector/usernames-and-emails-from-request who emails))))

           )
