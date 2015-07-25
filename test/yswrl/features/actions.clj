(ns yswrl.features.actions
  (:require [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [yswrl.test.scaffolding :as s]))
(defn log-out [session]
  (-> session
      (follow [:a.logout-link])
      (follow-redirect)))

(defn follow-login-link [session]
  (-> session
      (follow [:a.login-link])))

(defn follow-create-link [session]
  (-> session
      (follow [:a.create-link])))

(defn submit [session name]
  (-> session
      (press name)
      (follow-redirect)))

(defn save-swirl [session]
  (-> session
      (submit "Publish Swirl")))

(defn login-as [visit user]
  (-> visit
      (fill-in "Username or email" (user :username))
      (fill-in "Password" s/test-user-password)
      (press "Login")
      (follow-redirect)))

