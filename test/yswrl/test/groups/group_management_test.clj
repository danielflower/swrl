(ns yswrl.test.groups.group-management-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.groups.group-management :as mgmt]
            [yswrl.groups.groups-repo :as repo]
            [korma.core
             :refer [insert values where join fields set-fields select raw modifier]])
  (:use clojure.test))


(deftest group-management-test
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
      (is (= sometwos-groups [group]))
      (is (= [(:username someone) (:username sometwo)] (map :username (repo/get-group-members (group :id)))))))

  (testing "Getting a group returns nil if the user is not part of the group"
    (let [member (create-test-user)
          non-member (create-test-user)
          group (repo/create-group (member :id) "Exclusive" "This is a group is what it is")
          _ (repo/add-group-member (group :id) (member :id))]

      (is (= group (repo/get-group-if-allowed (group :id) (member :id))))
      (is (nil? (repo/get-group-if-allowed (group :id) (non-member :id))))))

  (testing "can associate a swirl with a group"
    (let [author (create-test-user)
          member (create-test-user)
          group (repo/create-group (author :id) "Exclusive" "This is a group is what it is")
          _ (repo/add-group-member (group :id) (member :id))
          swirl (create-swirl "website" (author :id) "This is my swirl" "And I like it" [])]

      (repo/set-swirl-links (swirl :id) (author :id) [(group :id)])
      (is (= [group] (repo/get-groups-linked-to-swirl (swirl :id))))

      ; adding again does nothing
      (repo/set-swirl-links (swirl :id) (author :id) [(group :id)])
      (is (= [group] (repo/get-groups-linked-to-swirl (swirl :id))))

      ; the swirls can be looked up
      (is (= [(swirl :id)] (map :id (repo/get-swirls-for-group (group :id) member))))

      ; swirls not supplied will be removed
      (repo/set-swirl-links (swirl :id) (author :id) [])
      (is (= [] (repo/get-groups-linked-to-swirl (swirl :id))))

      ))

  )