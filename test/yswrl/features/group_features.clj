(ns yswrl.features.group-features
  (:require [yswrl.handler :refer [app]]
            [yswrl.test.scaffolding :as s]
            [yswrl.features.actions :as actions]
            [kerodon.core :refer :all]
            [kerodon.impl :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]
            [yswrl.links :as links])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))
(use 'korma.core)
(use 'korma.db)

(selmer.parser/cache-off!)

(defn now [] (System/currentTimeMillis))

(defn save-state [session map key value]
  (swap! map (fn [old-val] (assoc old-val key value)))
  session)

(defn save-url [session map key]
  (let [url (get-in session [:request :uri])]
    (save-state session map key url))
  session)


(deftest group-creation
  (with-faked-responses
    (let [owner (s/create-test-user)
          member (s/create-test-user)
          non-member (s/create-test-user)
          test-state (atom {})]

      (-> (session app)
          (visit "/swirls/groups")
          ; Login as user 1
          (actions/follow-login-link)
          (actions/login-as owner)
          (visit "/swirls/groups")

          ; Create a group
          (follow "Create a group")
          (fill-in "Group name" "Going to change this")
          (fill-in "Description" "Just for my special friends")
          ;(fill-in :.recipients (non-member :username))
          (actions/submit "Save group")

          (follow "Edit group")
          (fill-in "Group name" "My special group")
          ;(uncheck (non-member :username))
          (fill-in :.recipients (member :username))
          (actions/submit "Save group")

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
          (within [:.join-url]
                  (has (some-text? (str "http://www.swrl.co" (@test-state :group-url) "/join/"))))

          ; Create a swirl
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")

          (fill-in :#swirl-title "Special Swirl")
          (check "My special group")

          (actions/save-swirl)

          ; Go back to the group page via the link that is in the homepage and click the swirl
          (visit "/swirls/groups")
          (follow "My special group")
          (follow "Special Swirl")

          ; A swirl can also be created by pressing the big pink button and it will be shared with the group and by private by default
          (visit "/swirls/groups")
          (follow "My special group")
          (follow :.bpb)
          (fill-in "Album or Song Title" "Mellon Collie")
          (press :#album-search-go-button)
          (follow "Mellon Collie and the Infinite Sadness (Remastered)")
          (follow-redirect)
          (actions/save-swirl)
          (within [:.private-swirl-message] (has (some-text? "This is a private Swirl so only those invited can see it.")))
          (visit "/swirls/groups")
          (follow "My special group")
          (follow "Mellon Collie and the Infinite Sadness (Remastered)")

          ; A member is notified and can view the group
          (actions/log-out)
          (actions/follow-login-link)
          (actions/login-as member)
          (visit (links/notifications))
          (follow "a new group")
          ;(visit (@test-state :group-url))
          (has (status? 200))
          (within [:h1]
                  (has (text? "My special group")))

          ; But non-members can't
          (actions/log-out)
          (actions/follow-login-link)
          (actions/login-as non-member)
          (visit (@test-state :group-url))
          (has (status? 404))

          ))))


(deftest group-joining
  (with-faked-responses
    (let [owner (s/create-test-user)
          member (s/create-test-user)
          group (s/create-group "Some group" "Some description" owner)
          join-url (links/join-group group)]

      (-> (session app)
          (visit join-url)
          (follow-redirect)
          (actions/login-as member)
          (follow-redirect)
          (within [:h1]
                  (has (text? "Some group")))


          ; invalid codes lead to 404 errors
          (visit (links/join-group (group :id) 1234))
          (has (status? 404))

          ))))