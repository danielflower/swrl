(ns yswrl.swirls.postman
  (:require [clojure.tools.logging :as log]
            [yswrl.db :as db]
            [korma.core
             :refer [select* limit subselect offset order
                     aggregate defentity database prepare transform table exec-raw
                     insert values where join fields set-fields select raw modifier]]
            [clojure.data.json :as json]
            [clj-http.client :as client]))
(use 'selmer.parser)

(def postmark-api-key-or-nil
  (let [env-var-name "POSTMARK_API_TOKEN"
        key (System/getenv env-var-name)]
    (if (clojure.string/blank? key)
      (do
        (log/info "Skipping email sending as the Postmark key is not set as an environment value with key" env-var-name)
        (log/info "You can set" env-var-name "to POSTMARK_API_TEST in your env vars to test sending to Postmark without actually delivering emails")
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
      (if (nil? postmark-api-key-or-nil)
        [{:email "", :status "error", :reject_reason "Postmark not configured"}]
        (let [response (client/post
                         "https://api.postmarkapp.com/email"
                         {:headers {"Accept"                  "application/json"
                                    "Content-Type"            "application/json"
                                    "X-Postmark-Server-Token" postmark-api-key-or-nil}
                          :body    (json/write-str {:HtmlBody body
                                                    :Subject  subject
                                                    ; if you change sender name or email, you need to create a new signiture in postmark dashboard. Maybe just don't do it.
                                                    :From     "Swrl <feedback@swrl.co>"
                                                    :To       to-email})
                          :throw-exceptions false})
              result (json/read-str (response :body)
                                    :eof-error? false
                                    :eof-value {:ErrorCode -1 :Message (str "Could not parse result. Response code " (response :status))}
                                    :key-fn clojure.core/keyword)
              status (if (= 0 (result :ErrorCode))
                       "sent"
                       "error")]
          {:_id           (result :MessageID)
           :email         (result :To)
           :status        status
           :reject_reason (result :Message)})))))
