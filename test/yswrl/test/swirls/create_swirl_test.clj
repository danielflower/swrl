(ns yswrl.test.swirls.create-swirl-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.user.networking :as networking]
            [yswrl.db :as db]
            [yswrl.swirls.lookups :as lookups])
  (:use clojure.test))

(deftest create-swirl-test

  (let [author (create-test-user)
        friend (create-test-user)]

    (testing "A user can create a swirl and selected users will be included"
      (let [
            unregistered-user-email (str "jondoe" (System/currentTimeMillis) "@example.org")
            created (create-swirl "generic" (author :id) "Some thing" "Boz it's really <b>great</b>.", [(friend :username) unregistered-user-email])
            retrieved (lookups/get-swirl (created :id))]
        (is (= (retrieved :title) "Some thing"))
        (is (= (retrieved :review) "Boz it's really <b>great</b>."))
        (is (db/exists? "SELECT 1 FROM suggestions WHERE swirl_id = ? AND recipient_email = ? AND recipient_id IS NULL", (created :id) unregistered-user-email))
        (is (db/exists? "SELECT 1 FROM suggestions WHERE swirl_id = ? AND recipient_id = ? AND recipient_email IS NULL", (created :id) (friend :id)))
        (is (= (networking/get-relations (author :id) :knows) [(user-to-relation friend)]))
        (is (= (networking/get-relations (friend :id) :knows) [(user-to-relation author)]))
        (is (nil? (retrieved :itunes_album_id)))

        ))

    (testing "Adding the same users again causes no issues"
      (create-swirl "generic" (author :id) "Thing 1" "I'm thing 1", [(friend :username) "someoneelse@example.org"])
      (create-swirl "generic" (author :id) "Thing 2" "And this is thing 2", [(friend :username) "someoneelse@example.org"]))

    ))
