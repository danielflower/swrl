(ns yswrl.test.user.networking-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.user.networking :as networking])
  (:use clojure.test))

(deftest networking

  (let [user1 (create-test-user)
        user2 (create-test-user)
        user3 (create-test-user)
        ids-1-and-2 [(user1 :id) (user2 :id)]]

    (testing "A relation between two users can be created and changed"
      (networking/store (user1 :id) :knows (user2 :id))
      (is (= [(user2 :id)] (networking/get-relations (user1 :id) :knows)))
      (networking/store (user1 :id) :ignores (user2 :id))
      (is (empty? (networking/get-relations (user1 :id) :knows)))
      (is (= [(user2 :id)] (networking/get-relations (user1 :id) :ignores))))

    (testing "Multiple relations can be added together"
      (networking/store-multiple (user3 :id) :knows ids-1-and-2)
      (is (= ids-1-and-2 (networking/get-relations (user3 :id) :knows)))
      (networking/store-multiple (user3 :id) :ignores ids-1-and-2)
      (is (empty? (networking/get-relations (user3 :id) :knows)))
      (is (= ids-1-and-2 (networking/get-relations (user3 :id) :ignores))))

    ))