(ns yswrl.features.swirl-weightings-test
  (:require [yswrl.handler :refer [app]]
            [yswrl.test.scaffolding :as s]
            [net.cgrand.enlive-html :as enlive]
            [yswrl.links :as linky]
            [yswrl.features.actions :as actions]
            [kerodon.core :refer :all]
            [kerodon.impl :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.db :as db]
            [yswrl.links :as links]
            [yswrl.swirls.lookups :as lookups]
            [yswrl.features.swirl-features :as features]
            [korma.core
             :refer [insert values where join fields set-fields select raw modifier]])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))
(use 'korma.db)

(selmer.parser/cache-off!)

(defn assert-swirl-weightings-value [session swirl-id user-id weighting-to-check value-to-check]
  (is (= value-to-check
         (-> (select db/swirl-weightings
                     (fields weighting-to-check)
                     (where {:swirl_id swirl-id
                             :user_id  user-id}))
             first
             weighting-to-check)))
  session)

(deftest weightings-table-gets-updated-correctly
  (with-faked-responses
    (let [user1 (s/create-test-user)
          user2 (s/create-test-user)
          test-state (atom {})]

      (-> (session app)
          (visit "/")
          ; Login as user 1
          (actions/follow-login-link)
          (features/login-as user1)

          ; Create a swirl
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")

          (actions/save-swirl)
          (features/save-swirl-id test-state :swirl-id)
          (features/save-url test-state :view-swirl-uri)

          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :is_author true)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :is_author false)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :is_recipient false)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :is_recipient false)

          (features/assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

          ; recipient weightings

          (visit (@test-state :view-swirl-uri))
          (follow "Edit Swirl")
          (fill-in :.recipients (:email user2))
          (actions/save-swirl)

          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :is_recipient true)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :author_is_friend true)

          (visit (@test-state :view-swirl-uri))
          (actions/submit [(enlive/attr= :value "Loved it")])

          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :has_responded true)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :has_responded false)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :number_of_positive_responses 1)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :number_of_positive_responses 1)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :number_of_positive_responses_from_friends 0)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :number_of_positive_responses_from_friends 1)

          ; multiple positive responses don't count up the value
          (actions/submit [(enlive/attr= :value "Loved it")])
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :number_of_positive_responses 1)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :number_of_positive_responses 1)

          ;let's add a comment
          (fill-in :.editor "a comment")
          (actions/submit [(enlive/attr= :value "Add comment")])

          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :number_of_comments 1)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :number_of_comments_from_friends 0)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :number_of_comments 1)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :number_of_comments_from_friends 1)

          ;let's add another comment!
          (fill-in :.editor "a comment")
          (actions/submit [(enlive/attr= :value "Add comment")])

          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :number_of_comments 2)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user1) :number_of_comments_from_friends 0)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :number_of_comments 2)
          (assert-swirl-weightings-value (@test-state :swirl-id) (:id user2) :number_of_comments_from_friends 2)

          ))))

