(ns yswrl.db.core
  (:use korma.core
        [korma.db :only (defdb)]))

(def db-spec
  {:subprotocol "postgresql"
   :subname "//localhost/yswrl"
   :user "dev"
   :password "password"})

(defdb db db-spec)

(defentity users
           (database db)
           )

(defn create-user [username email password]
  (insert users
          (values { :username username :email email :password password :admin false :is_active true  } )))

(defn get-user [username]
  (first (select users
                 (where {:username username })
                 (limit 1))))
