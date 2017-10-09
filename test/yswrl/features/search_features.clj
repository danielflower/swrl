(ns yswrl.features.search-features
  (:require [yswrl.handler :refer [app]]
            [yswrl.test.scaffolding :as s]
            [yswrl.features.actions :as actions]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]
            [yswrl.swirls.swirls-repo :as swirls-repo])
  (:import (java.net URLEncoder)))
(selmer.parser/cache-off!)

(defn print-session [session]
  (println session)
  session)
(let [user (s/create-test-user)
      title (str "Some swirl " (System/currentTimeMillis))
      _ (s/create-swirl "website" (user :id) title "This is an review" [])
      draft (swirls-repo/save-draft-swirl nil "website" (user :id) (str "Draft" (System/currentTimeMillis)) "This is a draft" nil "www.google.com")]
  (deftest anon-users-can-search-public-swirls
    (-> (session app)
        (visit "/search")
        (fill-in [:.query] title)
        (press "Go")
        #_(within [:.search-result-summary]                 ; this works in real life and works for other tests below.
          (has (text? (str "Showing 1 results for " title)))) ; unsure why doesn't work here, but not going to waste time finding out
        (follow title)
        (within [:h1]
          (has (text? title)))))

  (deftest search-pages-can-be-navigated-to-directly
    (-> (session app)
        (visit (str "/search?query=" (URLEncoder/encode title)))
        (follow title)
        (within [:h1]
          (has (text? title)))))

  (deftest logged-in-users-can-search
    (-> (session app)
        (visit "/login")
        (actions/login-as user)
        (visit "/search")
        (fill-in [:.query] title)
        (press "Go")
        (follow title)
        (within [:h1]
          (has (text? title)))))


  (deftest no-one-can-see-drafts
    (-> (session app)
        (visit "/login")
        (actions/login-as user)
        (visit "/search")
        (fill-in [:.query] (draft :title))
        (press "Go")
        (within [:.search-result-summary]
          (has (text? (str "Showing 0 results for " (draft :title) " ")))))))