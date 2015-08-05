(ns yswrl.groups.groups-repo
  (:require
    [yswrl.db :as db]
    [yswrl.swirls.lookups :as lookups]))

(use 'korma.core)

(defn create-group [creator-id group-name description]
  (insert db/groups
          (values {:name group-name :created_by_id creator-id :description description :join_code (Math/round (* (Math/random) Integer/MAX_VALUE))})))

(defn add-group-member [group-id user-id]
  (insert db/group-members
          (values {:group_id group-id :user_id user-id})))

(defn multiple-groups []
  (-> (select* db/groups)
      (fields :id :name :date_created :created_by_id :description :join_code)
      (order :name :asc)
      ))

(defn get-swirls-for-group [group-id requestor]
  (-> (lookups/select-multiple-swirls requestor 500 0)
      (join :inner db/group-swirl-links (= :swirls.id :group_swirl_links.swirl_id))
      (where {:group_swirl_links.group_id group-id})
      (select)))

(defn get-groups-for [user-id]
  (-> (multiple-groups)
      (join :inner db/group-members (= :groups.id :group_members.group_id))
      (where {:group_members.user_id user-id})
      (select)))

(defn get-groups-linked-to-swirl [swirl-id]
  (-> (multiple-groups)
      (join :inner db/group-swirl-links (= :groups.id :group_swirl_links.group_id))
      (where {:group_swirl_links.swirl_id swirl-id})
      (select)))

(defn set-swirl-links [swirl-id added-by-id group-ids]
  (let [group-set (set group-ids)
        current (set (map :id (get-groups-linked-to-swirl swirl-id)))
        to-add (clojure.set/difference group-set current)   ; items selected but not already in the current set
        to-delete (clojure.set/difference current group-set) ; any current ones which weren't passed in
        ]
    (doseq [group-id to-add]
      (insert db/group-swirl-links
              (values {:group_id group-id :swirl_id swirl-id :added_by_id added-by-id})))
    (doseq [group-id to-delete]
      (delete db/group-swirl-links
              (where {:group_id group-id :swirl_id swirl-id :added_by_id added-by-id})))))

(defn get-group-members [group-id]
  (select db/users
          (fields :id :username :email_md5)
          (join :inner db/group-members (= :users.id :group_members.user_id))
          (where {:group_members.group_id group-id})
          (order :id :asc)))

(defn get-group-if-allowed [group-id user-id]
  (first (-> (multiple-groups)
             (join :inner db/group-members (= :groups.id :group_members.group_id))
             (where {:id group-id :group_members.user_id user-id})
             (select))))

(defn get-group-by-code [group-id code]
  (first (-> (multiple-groups)
             (where {:id group-id :join_code code})
             (select))))

(defn update-group [group-id group-name group-description]
  (update db/groups
          (set-fields {:name group-name :description group-description})
          (where {:id group-id})))
