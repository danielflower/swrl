(ns yswrl.db.core)
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

