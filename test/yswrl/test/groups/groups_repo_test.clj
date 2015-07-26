(ns yswrl.test.groups.groups-repo-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.groups.groups-repo :as repo])
  (:use clojure.test))
(use 'korma.core)


(deftest groups-repo
  (testing "A user can create a group and add people to it"
    (let [owner (create-test-user)
          someone (create-test-user)
          sometwo (create-test-user)
          group (repo/create-group (owner :id) "Z group" "This is a group is what it is")
          group2 (repo/create-group (owner :id) "A group" "This is a group is what it is")
          _ (repo/add-group-member (group :id) (someone :id))
          _ (repo/add-group-member (group :id) (sometwo :id))
          _ (repo/add-group-member (group2 :id) (someone :id))
          someones-groups (repo/get-groups-for (someone :id))
          sometwos-groups (repo/get-groups-for (sometwo :id))]

      (is (= someones-groups [group2 group]))
      (is (= sometwos-groups [group]))))

  (testing "Getting a group returns nil if the user is not part of the group"
    (let [member (create-test-user)
          non-member (create-test-user)
          group (repo/create-group (member :id) "Exclusive" "This is a group is what it is")
          _ (repo/add-group-member (group :id) (member :id))]

      (is (= group (repo/get-group-if-allowed (group :id) (member :id))))
      (is (nil? (repo/get-group-if-allowed (group :id) (non-member :id))))))

  )