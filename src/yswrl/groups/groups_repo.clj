(ns yswrl.groups.groups-repo
  (:require
    [yswrl.db :as db]
    [yswrl.swirls.lookups :as lookups]
    [korma.core :as k]))


(defn create-group [creator-id group-name description]
  (k/insert db/groups
          (k/values {:name group-name :created_by_id creator-id :description description :join_code (Math/round (* (Math/random) Integer/MAX_VALUE))})))

(defn add-group-member [group-id user-id]
  (k/insert db/group-members
          (k/values {:group_id group-id :user_id user-id})))

(defn multiple-groups []
  (-> (k/select* db/groups)
      (k/fields :id :name :date_created :created_by_id :description :join_code)
      (k/order :name :asc)
      ))

(defn get-swirls-for-group [group-id requestor]
  (-> (lookups/select-multiple-swirls requestor 500 0)
      (k/join :inner db/group-swirl-links (= :swirls.id :group_swirl_links.swirl_id))
      (k/where {:group_swirl_links.group_id group-id})
      (k/select)))

(defn get-groups-for [user-id]
  (-> (multiple-groups)
      (k/join :inner db/group-members (= :groups.id :group_members.group_id))
      (k/where {:group_members.user_id user-id})
      (k/select)))

(defn get-groups-linked-to-swirl [swirl-id]
  (-> (multiple-groups)
      (k/join :inner db/group-swirl-links (= :groups.id :group_swirl_links.group_id))
      (k/where {:group_swirl_links.swirl_id swirl-id})
      (k/select)))

(defn set-swirl-links [swirl-id added-by-id group-ids]
  (let [group-set (set group-ids)
        current (set (map :id (get-groups-linked-to-swirl swirl-id)))
        to-add (clojure.set/difference group-set current)   ; items selected but not already in the current set
        to-delete (clojure.set/difference current group-set) ; any current ones which weren't passed in
        ]
    (doseq [group-id to-add]
      (k/insert db/group-swirl-links
              (k/values {:group_id group-id :swirl_id swirl-id :added_by_id added-by-id})))
    (doseq [group-id to-delete]
      (k/delete db/group-swirl-links
              (k/where {:group_id group-id :swirl_id swirl-id :added_by_id added-by-id})))))

(defn get-group-members [group-id]
  (k/select db/users
          (k/fields :id :username :email_md5)
          (k/join :inner db/group-members (= :users.id :group_members.user_id))
          (k/where {:group_members.group_id group-id})
          (k/order :id :asc)))

(defn get-group-if-allowed [group-id user-id]
  (first (-> (multiple-groups)
             (k/join :inner db/group-members (= :groups.id :group_members.group_id))
             (k/where {:id group-id :group_members.user_id user-id})
             (k/select))))

(defn get-group-by-code [group-id code]
  (first (-> (multiple-groups)
             (k/where {:id group-id :join_code code})
             (k/select))))

(defn update-group [group-id group-name group-description]
  (k/update db/groups
          (k/set-fields {:name group-name :description group-description})
          (k/where {:id group-id})))
