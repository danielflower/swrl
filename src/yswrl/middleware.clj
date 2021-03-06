(ns yswrl.middleware
  (:require [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [selmer.middleware :refer [wrap-error-page]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [noir-exception.core :refer [wrap-internal-error]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends.session :refer [session-backend]]
            [yswrl.links :as linky]
            [clojure.string :as str]))

(defn permanent-redirect
  "Returns a Ring response for an HTTP 301 redirect."
  [url]
  {:status  301
   :headers {"Location" url}
   :body    ""})

(defn log-request [handler]
  (fn [req]
    (log/debug req)
    (handler req)))

(defn development-middleware [handler]
  (if (env :dev)
    (-> handler
        wrap-error-page
        wrap-exceptions)
    handler))

(def unsecure-key-for-dev-mode "93762d738951e53a")          ; in heroku, a value similar to this is stored in an env var. It just needs to be any non-changing value

(def allowed-hosts #{"www.swrl.co" "localhost"})

(defn wrap-url-canonicalizer-policy [handler]
  (fn [req]
    (let [host (get (req :headers) "host")]
      (if (or (contains? allowed-hosts host) (not= 80 (req :server-port)))
        (handler req)
        (permanent-redirect (str (linky/absolute (req :uri))))))))


(defn get-unhandled-error-text [ex]
  (let [stack-strings (map #(str %) (.getStackTrace ex))]
    (str "Unhandled exception: " ex \newline " at " (clojure.string/join (str \newline "    ") stack-strings))))


(defn production-middleware [handler]
  (let [is-production (not (nil? (env :production)))]
    (-> handler
        (wrap-url-canonicalizer-policy)
        (wrap-authentication (session-backend))
        (wrap-restful-format :formats [:json-kw :edn :transit-json :transit-msgpack])
        (wrap-defaults
          (-> site-defaults
              (assoc-in [:session :store] (cookie-store {:key (or (System/getenv "SECRET_COOKIE_KEY") unsecure-key-for-dev-mode)}))
              (assoc-in [:session :cookie-name] "swirl-session")
              (assoc-in [:security :anti-forgery] false)
              (assoc-in [:security :hsts] is-production)
              (assoc-in [:security :ssl-redirect] is-production)
              (assoc-in [:security :xss-protection :enable?] false)
              (assoc-in [:proxy] true)
              ))
        (wrap-gzip)
        (wrap-internal-error :log #(log/error (get-unhandled-error-text %))))))
