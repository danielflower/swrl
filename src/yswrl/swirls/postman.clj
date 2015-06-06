(ns yswrl.swirls.postman
  (:require [clojure.tools.logging :as log]))
(use 'clj-mandrill.core)
(use 'selmer.parser)

(def mandrill-api-key-or-nil
  (let [env-var-name "MANDRILL_APIKEY"
        key (System/getenv env-var-name)]
    (if (clojure.string/blank? key)
      (do
        (log/warn "Skipping email sending as the Mandrill key is not set as an environment value with key" env-var-name)
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

(defn test-mandrill []
  (wrap-mandrill-call call-mandrill "users/ping" {}))

(defn send-email [to-list subject body]
  (wrap-mandrill-call send-message {
                 :html       body
                 :subject    subject
                 :from_email "noreply@swrl.co"
                 :from_name  "noreply@swrl.co"
                 :to         to-list}))
