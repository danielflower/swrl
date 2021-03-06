(ns yswrl.auth.guard
  (:require [ring.util.response :refer [redirect response not-found]]
            [yswrl.links :as links]
            [yswrl.auth.auth-repo :as auth-repo]
            [clojure.tools.logging :as log]
            [yswrl.rest.utils :as utils]))

(defn is-logged-in? [req]
  (not (nil? (get-in req [:session :user]))))

(defn requires-login [handler]
  (fn [request]
    (if (is-logged-in? request)
      (handler)
      (let [return-url (str (request :uri) "?" (request :query-string))]
        (redirect (str "/login?return-url=" (links/url-encode return-url)))))))

(defn requires-app-auth-token [handler]
  (fn [request]
    (let [id (get-in request [:params :user_id])
          id (if (string? id)
               (try (Integer/parseInt id)
                    (catch Exception e
                      (log/error e "Request for app api used an invalid user ID which couldn't be parsed to int")
                      nil))
               id)
          auth-token (get-in request [:params :auth_token])]
      (let [auth-token-from-db (try (auth-repo/get-app-auth-token-for-user {:id id})
                                    (catch Exception e
                                      (log/error e "Failed to verify app-auth-token for user. Request: " request)
                                      nil))]
        (if (and (not= nil auth-token-from-db) (= auth-token auth-token-from-db))
          (handler)
          (utils/json-response {:message "Auth-token is not valid"} 403)))
      )))