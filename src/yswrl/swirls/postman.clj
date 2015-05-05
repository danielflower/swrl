(ns yswrl.swirls.postman
  (:require [clojure.tools.logging :as log]))
(use 'clj-mandrill.core)
(use 'selmer.parser)



(defn email-body [template-path model]
    (render-file template-path model))

(defn wrap-mandrill-call [f & args]
    (let [env-var-name "MANDRILL_APIKEY"
          key (System/getenv env-var-name)]
      (if (clojure.string/blank? key)
        (do
          (log/warn "Skipping email sending as the Mandrill key is not set as an environment value with key" env-var-name)
          [{:email "", :status "error", :reject_reason "Mandrill not configured"}])
        (do
          (alter-var-root #'clj-mandrill.core/*mandrill-api-key* (constantly key))
          (apply f args)))))

(defn test-mandrill []
  (wrap-mandrill-call call-mandrill "users/ping" {}))

(defn send-email [to-list subject body]
  (wrap-mandrill-call send-message {
                 :html       body
                 :subject    subject
                 :from_email "noreply@youshouldwatchreadlisten.com"
                 :from_name  "noreply@youshouldwatchreadlisten.com"
                 :to         to-list}))
