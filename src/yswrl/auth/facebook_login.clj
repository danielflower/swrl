(ns yswrl.auth.facebook-login
  (:use compojure.core)
  (:require [clj-oauth2.client :as oauth2]
            [ring.util.response :refer [redirect]]
            [clj-http.client :as client]
            [cheshire.core :as parse]
            [yswrl.links :as links]
            [yswrl.auth.auth-routes :as routes])
  (:import (java.net URLEncoder)))


(def APP_ID "894319820613855")
(def APP_SECRET "b3ef0cc194e2abcfacd1ba32b085da2f")
(def REDIRECT_URI "http://localhost:3000/auth_facebook")

(defn facebook-oauth2 [return-url]
  {:authorization-uri "https://graph.facebook.com/oauth/authorize"
   :access-token-uri  "https://graph.facebook.com/oauth/access_token"
   :redirect-uri      REDIRECT_URI
   :client-id APP_ID
   :client-secret APP_SECRET
   :access-query-param :access_token
   :scope ["email"]
   :grant-type "authorization_code"}
   ;:state {return-url :return-url}
   )

(defn facebook [params]
  (let [access-token-response (:body (client/get (str "https://graph.facebook.com/oauth/access_token?"
                                                      "client_id=" APP_ID
                                                      "&redirect_uri=" REDIRECT_URI
                                                      "&client_secret=" APP_SECRET
                                                      "&code=" (get params "code"))))
        access-token (get (re-find #"access_token=(.*?)&expires=" access-token-response) 1)
        user-details (-> (client/get (str "https://graph.facebook.com/me?access_token=" access-token))
                         :body
                         (parse/parse-string))]
      (routes/attempt-thirdparty-login (get user-details "name") (get user-details "email") (get params "return-url") {} ))
      )


(defroutes facebook-routes
           (GET "/auth_facebook" {params :query-params} (facebook params))
           (GET "/facebook_login" [return-url] (redirect
                                       (:uri (oauth2/make-auth-request (facebook-oauth2 return-url))))))
