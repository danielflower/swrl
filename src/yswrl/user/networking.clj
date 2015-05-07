(ns yswrl.user.networking
  (:require [yswrl.db :as db])
  (:import (org.postgresql.util PSQLException)))
(use 'korma.core)


(defn store [user-id relation-type another-user-id]
  (try
    (insert db/network_connections (values {:user_id         user-id :relation_type (name relation-type)
                                            :another_user_id another-user-id}))
  (catch PSQLException e
    (if (.contains (.getMessage e) "duplicate key value violates unique constraint \"network_connections_uq\"")
      (update db/network_connections
              (set-fields {:relation_type (name relation-type)})
              (where {:user_id user-id :another_user_id :another_user_id}))
      (throw e)))))

(defn store-multiple [user-id relation-type user-ids-to-list]
  (doseq [another-id user-ids-to-list]
    (store user-id relation-type another-id)))

(defn get-relations [user-id relation-type]
  (map #(% :another_user_id)
       (select db/network_connections
               (fields :another_user_id)
               (where {:user_id user-id :relation_type (name relation-type)}))))