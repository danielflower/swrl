(ns yswrl.auth.auth-repo
  (:require [yswrl.db :refer [users]])
  )
(use 'korma.core)

(defn create-user [username email password]
  (insert users
          (values {:username username :email email :password password :admin false :is_active true})))

(defn get-user [username]
  (first (select users
                 (where {:username username})
                 (limit 1))))

(defn get-users-by-username_or_email [usernames]
  (select users
          (fields :id :username :email)
          (where (or {:username [in usernames]}
                     {:email [in usernames]}))
          ))
