(ns yswrl.swirls.postman)
(use 'clj-mandrill.core)






(defn wrap-mandrill-call [f & args]
    (let [env-var-name "MANDRILL_APIKEY"
          key (System/getenv env-var-name)]
      (if (clojure.string/blank? key)
        (do
          (println (str "Skipping email sending as the Mandrill key is not set as an environment value with key " env-var-name))
          nil)
        (do
          (alter-var-root #'clj-mandrill.core/*mandrill-api-key* (constantly key))
          (apply f args)))))

(defn test-mandrill []
  (wrap-mandrill-call call-mandrill "users/ping" {}))

(defn send-email [to-name to-email subject body]
  (wrap-mandrill-call send-message {
                 :html       body
                 :subject    subject
                 :from_email "noreply@youshouldwatchreadlisten.com"
                 :from_name  "noreply@youshouldwatchreadlisten.com"
                 :to         [{:email to-email :name to-name}]}))
