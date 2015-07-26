(ns yswrl.groups.groups-repo
  (:require
    [yswrl.db :as db]))

(use 'korma.core)

(defn create-group [creator-id group-name description]
  (insert db/groups
          (values {:name group-name :created_by_id creator-id :description description})))

(defn add-group-member [group-id user-id]
  (insert db/group-members
          (values {:group_id group-id :user_id user-id})))

(defn get-groups-for [user-id]
  (select db/groups
          (fields :id :name :date_created :created_by_id :description)
          (join :inner db/group-members (= :groups.id :group_members.group_id))
          (where {:group_members.user_id user-id})
          (order :name :asc)))

(defn get-group-if-allowed [group-id user-id]
  (first (select db/groups
                 (fields :id :name :date_created :created_by_id :description)
                 (join :inner db/group-members (= :groups.id :group_members.group_id))
                 (where {:id group-id :group_members.user_id user-id}))))
