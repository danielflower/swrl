(ns yswrl.features.group-features
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
            [yswrl.links :as links])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))
(use 'korma.core)
(use 'korma.db)

(selmer.parser/cache-off!)

(defn now [] (System/currentTimeMillis))



(defn login-as [visit user]
  (actions/login-as visit user))


(defn save-state [session map key value]
  (swap! map (fn [old-val] (assoc old-val key value)))
  session)

(defn save-url [session map key]
  (let [url (get-in session [:request :uri])]
    (save-state session map key url))
  session)

(defn save-swirl-id [session map key]
  (let [url (get-in session [:request :uri])
        [_ swirl-id] (re-find #".*/swirls/([\d]+)" url)
        swirl-id (Integer. swirl-id)]
    (save-state session map key swirl-id))
  session)

(defn assert-user-checkbox-is-checked [session user]
  (is (= "checked"
         (get-attr session [(enlive/attr= :value
                                          (user :username))] :checked))
      "User checkbox should be checked")
  session)

(defn assert-number-of-comments [session swirl-id number-to-check]
  (is (= number-to-check
         (count (repo/get-swirl-comments swirl-id))))
  session)

(defn assert-number-of-links [session swirl-id number-to-check]
  (is (= number-to-check
         (count (repo/get-links swirl-id))))
  session)


(defn assert-swirl-title-in-header [session verb title]
  (-> session
      (within [:h1]
              (has (text? title)))
      ))

(deftest group-creation
  (with-faked-responses
    (let [owner (s/create-test-user)
          member (s/create-test-user)
          non-member (s/create-test-user)
          test-state (atom {})]

      (-> (session app)
          (visit "/")
          ; Login as user 1
          (actions/follow-login-link)
          (login-as owner)
          (visit "/")

          ; Create a group
          (follow "Create a group")
          (fill-in "Group name" "My special group")
          (fill-in "Description" "Just for my special friends")
          (fill-in :.recipients (member :username))
          (actions/submit "Create group")

          ; View the group
          (within [:h1]
                  (has (text? "My special group")))
          (within [:.group-description]
                  (has (text? "Just for my special friends")))
          (within [:.group-members]
                  (has (some-text? (owner :username))))
          (within [:.group-members]
                  (has (some-text? (member :username))))
          (save-url test-state :group-url)

          ; Create a swirl
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")

          (fill-in :#swirl-title "Special Swirl")
          (check "My special group")

          (actions/save-swirl)

          ; Go back to the group page via the link that is in the template and click the swirl
          (follow "My special group")
          (follow "Special Swirl")

          ; A member can view the group
          (actions/log-out)
          (actions/follow-login-link)
          (login-as member)
          (visit (@test-state :group-url))
          (has (status? 200))

          ; But non-members can't
          (actions/log-out)
          (actions/follow-login-link)
          (login-as non-member)
          (visit (@test-state :group-url))
          (has (status? 404))

          ))))

