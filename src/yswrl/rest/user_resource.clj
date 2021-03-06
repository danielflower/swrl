(ns yswrl.rest.user-resource
  (:require
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.response :refer [status redirect response not-found]]
    [compojure.core :refer [defroutes context GET POST]]
    [yswrl.auth.auth-repo :as user-repo]
    [yswrl.rest.utils :as utils]
    [clojure.tools.logging :as log]
    [yswrl.user.networking :as network]
    [yswrl.auth.guard :as guard]))

(defroutes user-resource-rest-routes
           (GET "/api/v1/users" []
             (response (user-repo/users-for-dropdown-app)))


           (GET "/api/v1/contacts" [user_id]
             (guard/requires-app-auth-token #(response (network/get-relations (if (string? user_id)
                                                                                (Integer/parseInt user_id)
                                                                                user_id)
                                                                              :knows))))

           (GET "/api/v1/user-avatar" [user_id]
             (try (utils/json-response (user-repo/get-avatar-link-from-user-id (Integer/parseInt user_id) 200))
                  (catch Exception e
                    (log/info e "Failed to get user avatar for user_id: " user_id)
                    (utils/json-response {:message (str "Failed to get user avatar for user_id: " user_id)} 500)))))
