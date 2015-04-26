(ns yswrl.db.auth-repo
  (:require [yswrl.db.core :refer [users]])
  )
(use 'korma.core)

(defn create-user [username email password]
  (insert users
          (values {:username username :email email :password password :admin false :is_active true})))

(defn get-user [username]
  (first (select users
                 (where {:username username})
                 (limit 1))))
