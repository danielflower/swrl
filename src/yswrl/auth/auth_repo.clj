(ns yswrl.auth.auth-repo
  (:require [yswrl.db :refer [users]]
            [yswrl.db :as db]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :refer :all]
            [yswrl.user.networking :as networking]
            [korma.core :as k])
  )


(defn gravatar-code [email]
  (-> (hash/md5 (clojure.string/lower-case email))
      (bytes->hex)))

(defn create-user [username email password]
  (k/insert users
          (k/values {:username username :email email :password password :admin false :is_active true :email_md5 (gravatar-code email)})))

(defn change-password [user-id hashed-password]
  (k/update users
          (k/set-fields {:password hashed-password})
          (k/where {:id user-id})))

(defn get-user [username]
  (db/query-single "SELECT * FROM users WHERE LOWER(username) = ?" (clojure.string/lower-case username)))

(defn get-user-by-email [email]
  (db/query-single "SELECT * FROM users WHERE LOWER(email) = ?" (clojure.string/lower-case email)))

(defn get-user-by-id [id]
  (db/query-single "SELECT * FROM users WHERE id = ?" id))


(defn get-users-by-username_or_email [usernames]
  (if (not (empty? usernames))
    (let [lowered (map clojure.string/lower-case usernames)
          question-marks (->> (repeat (count lowered) "?")
                              (interpose ",")
                              (apply str))]
      (apply db/query (str "SELECT id, username, email FROM users WHERE LOWER(username) IN ( " question-marks " ) OR LOWER(email) IN ( " question-marks " )") (concat lowered lowered)))))

(defn user-exists [username]
  (db/exists? "SELECT 1 FROM users WHERE username = ?" username))


(defn user-exists-by-email [email]
  (db/exists? "SELECT 1 FROM users WHERE email = ?" email))


(defn- search-username [desired-name suffix]
  (loop [desired-name desired-name
         suffix suffix]
    (let [current (str desired-name suffix)]
      (if (user-exists current)
        (recur desired-name (inc suffix))
        current))))

(defn suggest-username [desired-name]
  (if (not (user-exists desired-name))
    desired-name
    (search-username desired-name 1)))

(defn migrate-suggestions-from-email [user-id user-email]
  (let [where-map {:recipient_email user-email}
        updates (k/select db/suggestions
                        (k/fields :swirls.author_id)
                        (k/join :inner db/swirls (= :swirls.id :suggestions.swirl_id))
                        (k/where where-map))]
    (networking/store-multiple user-id :knows (map #(% :author_id) updates))
    (k/update db/suggestions
            (k/set-fields {:recipient_id user-id :recipient_email nil})
            (k/where where-map))))

(defn users-for-dropdown []
  (k/select db/users
          (k/fields :username :email_md5)
          (k/order :username :asc)
          (k/limit 100)))

(defn update-user [user-id new-username new-email]
  (k/update db/users
          (k/set-fields {:username new-username :email new-email :email_md5 (gravatar-code new-email)})
          (k/where {:id user-id})))
