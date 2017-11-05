(ns yswrl.swirls.mailgun
  (:require [clojure.tools.logging :as log]
            [yswrl.db :as db]
            [korma.core
             :refer [select* limit subselect offset order
                     aggregate defentity database prepare transform table exec-raw
                     insert values where join fields set-fields select raw modifier]]
            [clojure.data.json :as json]
            [clj-http.client :as client]))
(use 'selmer.parser)

(def mailgun-api-key-or-nil
  (let [env-var-name "MAILGUN_API_KEY"
        key (System/getenv env-var-name)]
    (if (clojure.string/blank? key)
      (do
        (log/info "Skipping email sending as the Mailgun key is not set as an environment value with key" env-var-name)
        (log/info "You can set" env-var-name "to MAILGUN_API_KEY_TEST in your env vars to test sending to Mailgun without actually delivering emails")
        nil)
      key)))

(def mailgun-domain-or-nil
  (let [env-var-name "MAILGUN_DOMAIN"
        key (System/getenv env-var-name)]
    (if (clojure.string/blank? key)
      (do
        (log/info "Skipping email sending as the Mailgun domain is not set as an environment value with key" env-var-name)
        nil)
      key)))

(defn email-body [template-path model]
  (render-file template-path model))

(defn blacklist [email]
  (insert db/email-blacklist
          (values [{:email email}])))

(defn send-email [to-email subject body]
  (if (db/exists? "SELECT 1 FROM email_blacklist WHERE email = ?" to-email)
    [{:email "", :status "error", :reject_reason "Email is blacklisted"}]
    (do
      (if (or (nil? mailgun-domain-or-nil) (nil? mailgun-api-key-or-nil))
        [{:email "", :status "error", :reject_reason "Mailgun not configured"}]
        (let [response (client/post
                         (str "https://api.mailgun.net/v3/" mailgun-domain-or-nil "/messages")
                         {:accept           :json
                          :as               :json
                          :basic-auth       ["api" mailgun-api-key-or-nil]
                          :form-params      {:from    "Swrl <feedback@swrl.co>"
                                             :to      to-email
                                             :subject subject
                                             :html    body}
                          :throw-exceptions false})
              status (if (= 200 (:status response))
                       "sent"
                       "error")
              result {:_id           (:id (:body response))
                     :email         to-email
                     :status        status
                     :reject_reason (str (:status response) " " (:body response))}]
          (log/info "Email sent: " result)
          result)))))
