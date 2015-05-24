(ns yswrl.test.swirls.comment-notifier-test
  (:use clojure.test)
  (:require [yswrl.swirls.comment-notifier :as cn]))

(def some-swirl {:id 1 :title "This <great> thing"})
(def some-comment {:swirl_id 1})

(deftest comment-notifier
  (testing "an HTML template is created with links to the swirl"
    (let [html (cn/comment-notification-email-html some-swirl some-comment)]
      (is (.contains html (str "<a href=\"http://www.swrl.co/swirls/1\">This &lt;great&gt; thing</a>")) html))))


