(ns yswrl.test.user.networking-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.user.networking :as networking])
  (:use clojure.test))

(deftest networking

  (let [user1 (create-test-user)
        relation1 (user-to-relation user1)
        user2 (create-test-user)
        relation2 (user-to-relation user2)
        user3 (create-test-user)]

    (testing "A relation between two users can be created and changed"
      (networking/store (user1 :id) :knows (user2 :id))
      (is (= [relation2] (networking/get-relations (user1 :id) :knows)))
      (networking/store (user1 :id) :ignores (user2 :id))
      (is (empty? (networking/get-relations (user1 :id) :knows)))
      (is (= [relation2] (networking/get-relations (user1 :id) :ignores))))

    (testing "Multiple relations can be added together"
      (networking/store-multiple (user3 :id) :knows [(user1 :id) (user2 :id)])
      (is (equal-ignoring-order? [relation1 relation2] (networking/get-relations (user3 :id) :knows)))
      (networking/store-multiple (user3 :id) :ignores [(user1 :id) (user2 :id)])
      (is (empty? (networking/get-relations (user3 :id) :knows)))
      (is (equal-ignoring-order? [relation1 relation2] (networking/get-relations (user3 :id) :ignores))))

    ))