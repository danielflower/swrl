(ns yswrl.db
  (:require [clojure.set :refer [rename-keys]]))
(use 'korma.db)
(use 'korma.core)


(def db-uri (or (System/getenv "DATABASE_URL")
                "postgres://dev:password@localhost:5432/yswrl"))

(defn convert-db-uri [db-uri]
  (let [[_ user password host port db] (re-matches #"postgres://(?:(.+):(.*)@)?([^:]+)(?::(\d+))?/(.+)" db-uri)]
    {
     :user     user
     :password password
     :host     host
     :port     (or port 5432)
     :db       db
     }))


(defdb db (postgres (convert-db-uri db-uri)))

(defentity users (database db))

(defentity swirls (database db)
           (prepare (fn [v] (rename-keys v {:itunes-collection-id :itunes_collection_id})))
           (transform (fn [v] (rename-keys v {:itunes_collection_id :itunes-collection-id}))))
(defentity suggestions (database db))
(defentity swirl-links (table :swirl_links) (database db))
(defentity swirl-responses (table :swirl_responses) (database db))
(defentity comments (database db))
(defentity email-blacklist (table :email_blacklist) (database db))
(defentity notifications (database db))
(defentity password-reset-requests
           (table :password_reset_requests)
           (database db))
(defentity network-connections
           (table :network_connections)
           (database db))

(defn execute [sql & args]
  (exec-raw db [sql args]))

(defn query [sql & args]
  (exec-raw db [sql args] :results))

(defn query-single [sql & args]
  (first (apply query sql args)))

(defn exists? [sql & args]
  (> (count (apply query sql args)), 0))