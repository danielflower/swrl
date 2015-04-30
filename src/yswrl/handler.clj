(ns yswrl.handler
  (:require [compojure.core :refer [defroutes routes]]
            [yswrl.home.home-routes :refer [home-routes]]
            [yswrl.auth.auth-routes :refer [auth-routes]]
            [yswrl.swirls.swirl-routes :refer [swirl-routes]]
            [yswrl.swirls.suggestion-job :refer [send-unsent-suggestions-job]]
            [yswrl.middleware
             :refer [development-middleware production-middleware]]
            [yswrl.session :as session]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [cronj.core :as cronj]))

(defroutes base-routes
           (route/resources "/")
           (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []

  (if (env :dev) (parser/cache-off!))
  ;;start the expired session cleanup job
  (cronj/start! session/cleanup-job)
  (cronj/start! send-unsent-suggestions-job)
  (timbre/info "\n-=[ yswrl started successfully"
               (when (env :dev) "using the development profile") "]=-"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "yswrl is shutting down...")
  (cronj/shutdown! session/cleanup-job)
  (cronj/shutdown! send-unsent-suggestions-job)
  (timbre/info "shutdown complete!"))

(def app
  (-> (routes
        home-routes
        auth-routes
        swirl-routes
        base-routes)
      development-middleware
      production-middleware))
