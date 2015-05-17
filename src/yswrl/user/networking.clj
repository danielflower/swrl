(ns yswrl.user.networking
  (:require [yswrl.db :as db]))
(use 'korma.core)


(defn store [user-id relation-type another-user-id]
  (let [updated-count (update db/network-connections
                              (set-fields {:relation_type (name relation-type)})
                              (where {:user_id user-id :another_user_id another-user-id}))]
    (if (= 0 updated-count)
      (insert db/network-connections (values {:user_id         user-id :relation_type (name relation-type)
                                              :another_user_id another-user-id})))
    ))

(defn store-multiple [user-id relation-type user-ids-to-list]
  (doseq [another-id user-ids-to-list]
    (store user-id relation-type another-id)))

(defn get-relations [user-id relation-type]
  (select db/network-connections
          (fields [:another_user_id :user-id] :users.username)
          (join :inner db/users (= :users.id :network_connections.another_user_id))
          (where {:user_id user-id :relation_type (name relation-type)})))