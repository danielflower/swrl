(ns yswrl.test.swirls.create-swirl-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.user.networking :as networking]
            [yswrl.db :as db])
  (:use clojure.test))

(deftest create-swirl

  (let [author (create-test-user)
        friend (create-test-user)]

    (testing "A user can create a swirl and selected users will be included"
      (let [
            created (repo/create-swirl (author :id) "Some thing" "Boz it's really <b>great</b>.", [(friend :username) "someoneelse@example.org"])
            retrieved (repo/get-swirl (created :id))]
        (is (= (retrieved :title) "Some thing"))
        (is (= (retrieved :review) "Boz it's really <b>great</b>."))
        (is (db/exists? "SELECT 1 FROM suggestions WHERE swirl_id = ? AND recipient_email = ? AND recipient_id IS NULL", (created :id) "someoneelse@example.org"))
        (is (db/exists? "SELECT 1 FROM suggestions WHERE swirl_id = ? AND recipient_id = ? AND recipient_email IS NULL", (created :id) (friend :id)))
        (is (= (networking/get-relations (author :id) :knows) [(user-to-relation friend)]))
        (is (= (networking/get-relations (friend :id) :knows) [(user-to-relation author)]))

        ))

    (testing "Adding the same users again causes no issues"
      (repo/create-swirl (author :id) "Thing 1" "I'm thing 1", [(friend :username) "someoneelse@example.org"])
      (repo/create-swirl (author :id) "Thing 2" "And this is thing 2", [(friend :username) "someoneelse@example.org"]))

    ))