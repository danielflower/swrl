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

(defentity users
           (database db)
           )

(defn create-user [username email password]
  (insert users
          (values {:username username :email email :password password :admin false :is_active true})))

(defn get-user [username]
  (first (select users
                 (where {:username username})
                 (limit 1))))
