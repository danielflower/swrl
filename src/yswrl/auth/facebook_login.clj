(ns yswrl.auth.facebook-login
  (:use compojure.core)
  (:require [clj-oauth2.client :as oauth2]
            [ring.util.response :refer [redirect]]
            [clj-http.client :as client]
            [cheshire.core :as parse]
            [clojure.tools.logging :as log]
            [yswrl.auth.auth-routes :as routes])
  )

(def APP_ID (or (System/getenv "FACEBOOK_APP_ID")
                "894319820613855"))
(def APP_SECRET (or (System/getenv "FACEBOOK_APP_SECRET")
                    "b3ef0cc194e2abcfacd1ba32b085da2f"))

(defn facebook_redirect_uri [req]
  #_(str (clojure.string/replace (req :scheme) #":" "") "://" (req :server-name) ":" (req :server-port) "/facebook_auth")
  "https://www.swrl.co/facebook_auth")

(defn facebook-oauth2 [req]
  {:authorization-uri  "https://graph.facebook.com/oauth/authorize"
   :access-token-uri   "https://graph.facebook.com/oauth/access_token"
   :redirect-uri       (facebook_redirect_uri req)
   :client-id          APP_ID
   :client-secret      APP_SECRET
   :access-query-param :access_token
   :scope              ["email"]
   :response_type      "code"
   :grant-type         "authorization_code"}
  )

(defn facebook-error [req error-message]
  (log/error "Facebook login failure: " error-message req)
  (routes/login-page :return-url (get-in req [:session :return-url]) :fb-errors true :error-message error-message))

(defn get-facebook-code [req]
  (get-in req [:query-params "code"]))

(defn get-facebook-access-token [code req]
  (let [access-token-response (:body (client/get (str "https://graph.facebook.com/oauth/access_token?"
                                                      "client_id=" APP_ID
                                                      "&redirect_uri=" (facebook_redirect_uri req)
                                                      "&client_secret=" APP_SECRET
                                                      "&code=" code)))]
    (log/debug "Facebook access token body:" access-token-response)
    (get (re-find #"access_token=(.*?)&expires=" access-token-response) 1))
  )

(defn get-facebook-user-details [access-token]
  (-> (client/get (str "https://graph.facebook.com/me?access_token=" access-token))
      :body
      (parse/parse-string))
  )

(defn handle-facebook-auth-response [req]
  (if-let [code (get-facebook-code req)]
    (if-let [access-token (get-facebook-access-token code req)]
      (if-let [user-details (get-facebook-user-details access-token)]
        (if-let [email (get user-details "email")]
          (if-let [username (get user-details "name")]
            (routes/attempt-thirdparty-login username email {:id_type :facebook_id
                                                             :id (get user-details "id")
                                                             :avatar_type "facebook"} (get-in req [:session :return-url]) req)
            (facebook-error req "Cannot get username from Facebook details"))
          (facebook-error req "Cannot get email from Facebook details"))
        (facebook-error req "Cannot get User Details from Facebook. Did you login and authorise the application?"))
      (facebook-error req "Cannot get the access token from Facebook. Did you login and authorise the application?"))
    (facebook-error req "Facebook Oauth unsuccessful. Did you login and authorise the application?"))
  )

(defn handle-facebook-login [return-url req]
  (let [return-url (routes/redirect-url return-url)
        newSession  (assoc (req :session) :return-url return-url)
        response (redirect
                   (:uri (oauth2/make-auth-request (facebook-oauth2 req))))]
    (-> response (assoc :session newSession)))
  )

(defroutes facebook-routes
           (GET "/facebook_auth" [:as req] (handle-facebook-auth-response req))
           (GET "/facebook_login" [return-url :as req] (handle-facebook-login return-url req))
           (POST "/facebook_login" [return-url :as req] (handle-facebook-login return-url req)))
