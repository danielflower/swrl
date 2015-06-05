(ns yswrl.auth.guard
  (:require [ring.util.response :refer [redirect response not-found]]
            [yswrl.links :as links]))

(defn is-logged-in? [req]
  (not (nil? (get-in req [:session :user]))))

(defn requires-login [handler]
  (fn [request]
    (if (is-logged-in? request)
      (handler)
      (let [return-url (str (request :uri) "?" (request :query-string))]
        (redirect (str "/login?return-url=" (links/url-encode return-url)))))))
