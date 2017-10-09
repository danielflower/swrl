(ns yswrl.rest.discover-swirls
  (:require
    [compojure.core :refer [defroutes context GET POST]]
    [yswrl.swirls.lookups :as lookups]
    [yswrl.rest.utils :as rest-utils]))

(defn get-all-public-swirls-route []
  (GET "/public" []
    (-> (lookups/get-all-swirls-with-details 500 0 nil)
        rest-utils/json-response)))

(defroutes discover-routes
           (context "/api/v1/discover" []
             (get-all-public-swirls-route)
             ))