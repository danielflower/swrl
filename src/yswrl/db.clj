(ns yswrl.db)
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
(defentity swirls (database db))
(defentity suggestions (database db))
(defentity swirl_responses (database db))
(defentity comments (database db))
(defentity password_reset_requests (database db))

(defn execute [sql & args]
  (exec-raw db [sql args]))

(defn query [sql & args]
  (exec-raw db [sql args] :results))

(defn query-single [sql & args]
  (first (apply query sql args)))