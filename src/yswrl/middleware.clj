(ns yswrl.middleware
  (:require [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [selmer.middleware :refer [wrap-error-page]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [noir-exception.core :refer [wrap-internal-error]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends.session :refer [session-backend]]
            ))



(defn log-request [handler]
  (fn [req]
    (timbre/debug req)
    (handler req)))

(defn development-middleware [handler]
  (if (env :dev)
    (-> handler
        wrap-error-page
        wrap-exceptions)
    handler))

(defn production-middleware [handler]
  (-> handler
      (wrap-authentication (session-backend))
      (wrap-restful-format :formats [:json-kw :edn :transit-json :transit-msgpack])
      (wrap-defaults
        (-> site-defaults
        (assoc-in [:session :store] (cookie-store {:key (or (System/getenv "SECRET_COOKIE_KEY") "93762d738951e53a")}))
        (assoc-in [:session :cookie-name] "yswrl-session")))
      (wrap-internal-error :log #(timbre/error %))))
