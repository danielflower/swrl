(ns yswrl.features.swirl-features
  (:require [yswrl.handler :refer [app]]
            [yswrl.test.scaffolding :as s]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]))

(defn now [] (System/currentTimeMillis))



(defn login-as [visit user]
  (-> visit
      (fill-in "Username" (user :username))
      (fill-in "Password" s/test-user-password)
      (press "Login")
      (follow-redirect)))

(defn save-url [session map key]
  (let [url (get-in session [:request :uri])]
    (swap! map (fn [old-val] (assoc old-val key url))))
  session)

(deftest swirl-security
  (let [user1 (s/create-test-user)
        user2 (s/create-test-user)
        test-state (atom {})]

    (let [session (session app)]
      (-> session
          (visit "/")
          ; Login as user 1
          (follow "Login")
          (login-as user1)

          ; Create a swirl
          (follow "Create")
          (fill-in "Enter a YouTube video URL" "https://www.youtube.com/watch?v=TllPrdbZ-VI")
          (press "Go")
          (follow-redirect)

          (save-url test-state :edit-swirl-uri)

          (press "Submit")
          (follow-redirect)

          (save-url test-state :view-swirl-uri)

          (within [:h1]
                  (has (text? "You should consume How to chop an ONION using CRYSTALS with Jamie Oliver")))

          (follow "Log out")
          (follow-redirect)

          (follow "Login")
          (login-as user2)

          ; Other users can view the swirl....
          (visit (@test-state :view-swirl-uri))
          (within [:h1]
                  (has (text? "You should consume How to chop an ONION using CRYSTALS with Jamie Oliver")))

          ; ...but they can't edit the page
          (visit (@test-state :edit-swirl-uri))
          (has (status? 404))
          (has (text? "Not Found"))

          ))))
