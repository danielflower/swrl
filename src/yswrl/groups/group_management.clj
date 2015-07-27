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
    [yswrl.db :as db]
    [yswrl.auth.auth-repo :as auth]))
(use 'korma.core)
(use 'korma.db)

(defn user-from-session [req] (:user (:session req)))

(defn view-group-page [group-id user]
  (if-let [group (repo/get-group-if-allowed group-id (user :id))]
    (layout/render "groups/view-group.html" {:pageTitle (group :name)
                                             :group group
                                             :swirls (repo/get-swirls-for-group group-id (user :id))
                                             :members (repo/get-group-members group-id)})))


(defn edit-group-page [user errors group-title group_description]
  (layout/render "groups/create-group.html" {:title              "Create a group"
                                             :suggested-contacts (network/get-relations (user :id) :knows)
                                             :other-contacts     (network/get-unrelated-users (user :id) 100 0)
                                             :errors             errors :group-title group-title :group-description group_description}))

(defn create-group [creator group-name group-description member-usernames-and-emails]
  (transaction
    (let [group (repo/create-group (creator :id) group-name group-description)
          found-users (auth/get-users-by-username_or_email (distinct member-usernames-and-emails))]
      (doseq [member found-users]
        (repo/add-group-member (group :id) (member :id)))
      (repo/add-group-member (group :id) (creator :id))
      (redirect (links/group (group :id))))))


(defroutes group-routes

           (GET "/groups/:id{[0-9]+}" [id :as req] (guard/requires-login #(view-group-page (Long/parseLong id) (user-from-session req))))

           (GET "/create-group" [:as req] (guard/requires-login #(edit-group-page (user-from-session req) nil nil nil)))
           (POST "/create-group" [group-name group-description who emails :as req] (guard/requires-login #(create-group (user-from-session req) group-name group-description (user-selector/usernames-and-emails-from-request who emails))))

           )
