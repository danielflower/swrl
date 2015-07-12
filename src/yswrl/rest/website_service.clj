(ns yswrl.rest.website-service
  (:require
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.response :refer [status redirect response not-found]]
    [compojure.core :refer [defroutes context GET POST]]
    [yswrl.swirls.website :as website]))

(defroutes website-rest-routes
           (GET "/api/v1/website-service/get-metadata" [url]
             (response (website/get-metadata url))))
