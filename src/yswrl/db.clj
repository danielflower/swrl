(ns yswrl.db
  (:require [clojure.set :refer [rename-keys]]
            [ragtime.jdbc :as raggy]
            [ragtime.repl :as repl]
            [korma.core
             :refer [defentity database prepare transform table exec-raw
                     insert values where join fields set-fields select raw modifier]]))
(use 'korma.db)


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

(defn ragtime-config []
  (let [db (convert-db-uri db-uri)
        resources-with-wrong-ids-on-windows (raggy/load-resources "migrations")]
    {:datastore  (raggy/sql-database {:connection-uri (str "jdbc:postgresql://" (db :host) ":" (db :port) "/" (db :db) "?user=" (db :user) "&password=" (db :password))})
     :migrations (map (fn [m] (raggy/sql-migration {:id (clojure.string/replace (:id m) #".*/" "") :down (:down m) :up (:up m)})) resources-with-wrong-ids-on-windows)}))

(defn update-db []
    (repl/migrate (ragtime-config)))
(defn rollback-db []
  (repl/rollback (ragtime-config)))

(defentity users (database db))

(defentity swirls (database db)
           (prepare (fn [v] (rename-keys v {:itunes-collection-id :itunes_collection_id})))
           (transform (fn [v] (rename-keys v {:itunes_collection_id :itunes-collection-id}))))
(defentity suggestions (database db))
(defentity swirl-links (table :swirl_links) (database db))
(defentity swirl-responses (table :swirl_responses) (database db))
(defentity swirl-lists (table :swirl_lists) (database db))
(defentity comments (database db))

(defentity groups (database db))
(defentity group-members (database db) (table :group_members))
(defentity group-swirl-links (database db) (table :group_swirl_links))

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