(ns yswrl.handler
  (:require [compojure.core :refer [defroutes routes]]
            [yswrl.home.home-routes :refer [home-routes]]
            [yswrl.auth.auth-routes :refer [auth-routes]]
            [yswrl.swirls.creation :refer [creation-routes]]
            [yswrl.auth.password-reset :refer [password-reset-routes]]
            [yswrl.swirls.swirl-routes :refer [swirl-routes]]
            [yswrl.auth.facebook-login :refer [facebook-routes]]
            [yswrl.user.preference-routes :refer [preference-routes]]
            [yswrl.groups.group-management :refer [group-routes]]
            [yswrl.rest.swirl-resource :refer :all]
            [yswrl.rest.search-service :refer :all]
            [yswrl.rest.website-service :refer [website-rest-routes]]
            [yswrl.user.notifications :refer [notification-routes]]
            [yswrl.rest.user-resource :as user-resource]
            [yswrl.middleware :refer [development-middleware production-middleware]]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [selmer.parser :as parser]
            [environ.core :refer [env]]))

(defn wrap-site-pages [handler]
  (ring.middleware.anti-forgery/wrap-anti-forgery
    (fn [request]
      (if-let [response (handler request)]
        (-> response
            (assoc-in [:headers "Content-Security-Policy"] "default-src 'self'; img-src *; frame-src *; child-src *; style-src 'self' 'unsafe-inline'; script-src 'self' www.google-analytics.com platform.twitter.com connect.facebook.net")
            (assoc-in [:headers "Cache-Control"] "private, no-transform"))))))

(defn wrap-api-routes [handler]
    (fn [request]
      (if-let [response (handler request)]
        (-> response
            (assoc-in [:headers "Cache-Control"] "private, no-transform")))))


(defn wrap-infinite-cache-policy [handler]
  (fn [request]
    (if-let [response (handler request)]
      (assoc-in response [:headers "Cache-Control"] "public, max-age=31556926, no-transform"))))

(defroutes base-routes
           (route/resources "/" {:mime-types {"map" "application/json"}})
           (wrap-infinite-cache-policy (route/resources "/immutable" {:root "immutable" :mime-types {"map" "application/json"}}))
           (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []


  (log/info "Running DB migration")
  (yswrl.db/update-db)
  (log/info "DB migration complete")
  (if (env :dev) (parser/cache-off!))
  (log/info "-=[ yswrl started successfully"
            (when (env :dev) "using the development profile") "]=-"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (log/info "shutdown complete!"))

(def app
  (-> (routes
        (wrap-api-routes swirl-rest-routes)
        (wrap-api-routes swirl-app-rest-routes)
        (wrap-api-routes website-rest-routes)
        (wrap-api-routes user-resource/user-resource-rest-routes)
        (wrap-api-routes swirl-search-routes)
        (wrap-site-pages home-routes)
        (wrap-site-pages auth-routes)
        (wrap-site-pages swirl-routes)
        (wrap-site-pages group-routes)
        (wrap-site-pages preference-routes)
        (wrap-site-pages creation-routes)
        (wrap-site-pages password-reset-routes)
        (wrap-site-pages facebook-routes)
        (wrap-site-pages notification-routes)
        base-routes)
      development-middleware
      production-middleware))
