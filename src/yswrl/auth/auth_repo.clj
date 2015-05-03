(ns yswrl.auth.auth-repo
  (:require [yswrl.db :refer [users]]
            [yswrl.db :as db])
  )
(use 'korma.core)

(defn create-user [username email password]
  (insert users
          (values {:username username :email email :password password :admin false :is_active true})))

(defn get-user [username]
  (db/query-single "SELECT * FROM users WHERE LOWER(username) = ?" (clojure.string/lower-case username)))

(defn get-users-by-username_or_email [usernames]
  (let [lowered (map clojure.string/lower-case usernames)
        question-marks (->> (repeat (count lowered) "?")
                            (interpose ",")
                            (apply str))]
    (apply db/query (str "SELECT id, username, email FROM users WHERE LOWER(username) IN ( " question-marks " ) OR LOWER(email) IN ( " question-marks " )" ) (concat lowered lowered))))
