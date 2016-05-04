(ns yswrl.features.swirl-list-features
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
            [korma.core
             :refer [insert values where join fields set-fields select raw modifier]])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))
(use 'korma.db)

(selmer.parser/cache-off!)

(defn now [] (System/currentTimeMillis))

(defn test-log [session message]
  (println message)
  session)

(defn print-session [session]
  (println session)
  session)

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
        swirl-id (Long/parseLong swirl-id)]
    (save-state session map key swirl-id))
  session)

(defn assert-user-checkbox-is-checked [session user]
  (is (= "checked"
         (get-attr session [(enlive/attr= :value
                                          (user :username))] :checked))
      "User checkbox should be checked")
  session)

(defn assert-default-selection-is-value [session selection]
  (is (= "selected"
         (get-attr session [(enlive/attr= :value selection)] :selected))
      (str "Selected item should be " selection))
  session)

(defn assert-is-private-checkbox-is-checked [session]
  (is (= "checked"
         (get-attr session [:.private-toggle] :checked))
      "Private Toggle should be checked")
  session)

(defn assert-is-private-checkbox-is-not-checked [session]
  (is (not (= "checked"
              (get-attr session [:.private-toggle] :checked)))
      "Private Toggle should not be checked")
  session)

(defn assert-number-of-comments [session swirl-id number-to-check]
  (is (= number-to-check
         (count (repo/get-swirl-comments swirl-id))))
  session)

(defn assert-number-of-links [session swirl-id number-to-check]
  (is (= number-to-check
         (count (repo/get-links swirl-id))))
  session)

(defn assert-swirl-type [session swirl-id type-to-check]
  (is (= type-to-check
         (:type (lookups/get-swirl swirl-id))))
  session)

(defn assert-swirl-state [session user swirl-id state-to-check]
  (is (= state-to-check
         (:state (first (filter #(= swirl-id (:id %)) (lookups/get-swirls-in-user-swrl-list user 100 0 user))))))
  session)

(defn assert-swirl-title-in-header [session verb title]
  (-> session
      (within [:h1]
              (has (some-text? title)))
      ))

(defn cannot-follow [session selector]
  (if (= :cannot-follow (try (follow session selector)
                             (catch IllegalArgumentException _
                               :cannot-follow)))
    session
    (do (println "Shouldn't be able to follow link: " selector)
        (throw Exception))))


(deftest adding-a-swirl-to-wishlist-then-changing-states
  (with-faked-responses
    (let [user (s/create-test-user)
          test-state (atom {})]

      (-> (session app)
          (visit "/")
          (actions/follow-login-link)
          (login-as user)


          (visit "/")
          (follow "Add to your Swrl list")

          (fill-in "Album or Song Title" "Mellon Collie")
          (press :#album-search-go-button)
          (follow "Mellon Collie and the Infinite Sadness (Remastered)")

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (fill-in :#swirl-title "Mellon Collie and the Infinite Sadness")
          (actions/save-swirl)

          (save-swirl-id test-state :swirl-id)

          (assert-swirl-state user (@test-state :swirl-id) "wishlist")

          (actions/respond-to-swirl test-state user "Listening")

          (assert-swirl-state user (@test-state :swirl-id) "consuming")

          (actions/respond-to-swirl test-state user "Loved it")

          (assert-swirl-state user (@test-state :swirl-id) "done")

          (actions/respond-to-swirl test-state user "Dismissed")

          (assert-swirl-state user (@test-state :swirl-id) "dismissed")

          ; Dismissing again (for some reason, maybe you really don't like it) doesn't cause an issue

          (actions/respond-to-swirl test-state user "Dismissed")

          (assert-swirl-state user (@test-state :swirl-id) "dismissed")

          ))))