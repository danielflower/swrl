(ns yswrl.user.networking
  (:require [yswrl.db :as db]
            [korma.core
             :as k
             :refer [insert values where join fields set-fields select raw modifier]]
            ))


(defn store [user-id relation-type another-user-id]
  (let [updated-count (k/update db/network-connections
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
          (fields [:another_user_id :user-id] :users.username :users.email_md5)
          (join :inner db/users (= :users.id :network_connections.another_user_id))
          (where {:user_id user-id :relation_type (name relation-type)})))

(defn get-unrelated-users [user-id max-results skip]
  (db/query "with all_users as (select * from users u),
known_users as
(select u.id from users u
join network_connections nc on nc.another_user_id = u.id
where ( nc.user_id = ? and nc.relation_type = 'knows'))
select * from all_users a
where a.id not in (select k.id from known_users k)
and a.id != ?
limit ?
offset ?" user-id user-id max-results skip))