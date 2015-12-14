(ns yswrl.swirls.postman
  (:require [clojure.tools.logging :as log]
            [yswrl.db :as db]
            [korma.core
             :refer [select* limit subselect offset order
                     aggregate defentity database prepare transform table exec-raw
                     insert values where join fields set-fields select raw modifier]]))
(use 'clj-mandrill.core)
(use 'selmer.parser)

(def mandrill-api-key-or-nil
  (let [env-var-name "MANDRILL_APIKEY"
        key (System/getenv env-var-name)]
    (if (clojure.string/blank? key)
      (do
        (log/info "Skipping email sending as the Mandrill key is not set as an environment value with key" env-var-name)
        nil)
      key)))

(defn email-body [template-path model]
  (render-file template-path model))

(defn wrap-mandrill-call [f & args]
  (if (nil? mandrill-api-key-or-nil)
    [{:email "", :status "error", :reject_reason "Mandrill not configured"}]
    (do
      (alter-var-root #'clj-mandrill.core/*mandrill-api-key* (constantly mandrill-api-key-or-nil))
      (apply f args))))

(defn blacklist [email]
  (insert db/email-blacklist
          (values [{:email email}])))

#_(defn test-mandrill []
  (wrap-mandrill-call call-mandrill "users/ping" {}))

(defn send-email [to-email to-name subject body]
  (if (db/exists? "SELECT 1 FROM email_blacklist WHERE email = ?" to-email)
    [{:email "", :status "error", :reject_reason "Email is blacklisted"}]
    (wrap-mandrill-call send-message {
                                      :html       body
                                      :subject    subject
                                      :from_email "feedback@swrl.co"
                                      :from_name  "feedback@swrl.co"
                                      :to         [{:email to-email :name to-name}]})))
