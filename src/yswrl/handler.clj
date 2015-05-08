(ns yswrl.handler
  (:require [compojure.core :refer [defroutes routes]]
            [yswrl.home.home-routes :refer [home-routes]]
            [yswrl.auth.auth-routes :refer [auth-routes]]
            [yswrl.swirls.creation :refer [creation-routes]]
            [yswrl.auth.password-reset :refer [password-reset-routes]]
            [yswrl.swirls.swirl-routes :refer [swirl-routes]]
            [yswrl.swirls.suggestion-job :refer [send-unsent-suggestions-job]]
            [yswrl.middleware
             :refer [development-middleware production-middleware]]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [cronj.core :as cronj]))

(defn wrap-content-security-policy [handler]
  (fn [request]
    (if-let [response (handler request)]
      (assoc-in response [:headers "Content-Security-Policy"] "default-src 'self'; img-src *; child-src https://*.youtube.com http://*.youtube.com"))))

(defn wrap-infinite-cache-policy [handler]
  (fn [request]
    (if-let [response (handler request)]
      (assoc-in response [:headers "Cache-Control"] "max-age=31556926"))))

(defroutes base-routes
           (route/resources "/")
           (wrap-infinite-cache-policy (route/resources "/immutable" {:root "immutable"}))
           (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []

  (if (env :dev) (parser/cache-off!))
  (cronj/start! send-unsent-suggestions-job)
  (log/info "-=[ yswrl started successfully"
               (when (env :dev) "using the development profile") "]=-"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (log/info "yswrl is shutting down...")
  (cronj/shutdown! send-unsent-suggestions-job)
  (log/info "shutdown complete!"))

(def app
  (-> (routes
        (wrap-content-security-policy home-routes)
        (wrap-content-security-policy auth-routes)
        (wrap-content-security-policy swirl-routes)
        (wrap-content-security-policy creation-routes)
        (wrap-content-security-policy password-reset-routes)
        base-routes)
      development-middleware
      production-middleware))
