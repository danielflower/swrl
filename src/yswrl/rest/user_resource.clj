(ns yswrl.rest.user-resource
  (:require
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.response :refer [status redirect response not-found]]
    [compojure.core :refer [defroutes context GET POST]]
    [yswrl.auth.auth-repo :as user-repo]))

(defroutes user-resource-rest-routes
           (GET "/api/v1/users" []
             (response (user-repo/users-for-dropdown))))
