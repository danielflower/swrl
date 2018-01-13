(ns yswrl.rest.discover-swirls
  (:require
    [compojure.core :refer [defroutes context GET POST]]
    [yswrl.swirls.lookups :as lookups]
    [yswrl.rest.utils :as rest-utils]
    [yswrl.auth.guard :as guard]
    [yswrl.auth.auth-repo :as auth-repo]))

(defn get-all-public-swirls-route []
  (GET "/public" []
    (-> (lookups/get-all-swirls-with-details 5000 0 nil)
        rest-utils/json-response)))

(defn get-weighted-route []
  (GET "/weighted" [user_id]
    (guard/requires-app-auth-token
      #(-> (lookups/get-weighted-swirls-with-external-id 5000 0 (auth-repo/get-user-by-id (if (string? user_id)
                                                                                            (Integer/parseInt user_id)
                                                                                            user_id)))
           rest-utils/json-response))))

(defn get-inbox-route []
  (GET "/inbox" [user_id]
    (guard/requires-app-auth-token
      #(-> (lookups/get-swirls-awaiting-response-with-external-id (auth-repo/get-user-by-id (if (string? user_id)
                                                                                              (Integer/parseInt user_id)
                                                                                              user_id))
                                                                  5000 0)
           rest-utils/json-response))))



(defn get-swrls-responded-to []
  (GET "/responses" [user_id]
    (guard/requires-app-auth-token
      #(-> (lookups/get-responded-to-swirls-with-external-id (auth-repo/get-user-by-id (if (string? user_id)
                                                                                        (Integer/parseInt user_id)
                                                                                        user_id))
                                                            5000
                                                            0)
           rest-utils/json-response))))

(defn get-swrls-by-response-route []
  (GET "/responses/:response" [user_id response]
    (guard/requires-app-auth-token
      #(-> (lookups/get-swirls-by-response-with-external-id (auth-repo/get-user-by-id (if (string? user_id)
                                                                                        (Integer/parseInt user_id)
                                                                                        user_id))
                                                            5000
                                                            0
                                                            response)
           rest-utils/json-response))))

(defroutes discover-routes
           (context "/api/v1/discover" []
             (get-all-public-swirls-route)
             (get-weighted-route)
             (get-inbox-route)
             (get-swrls-by-response-route)
             ))