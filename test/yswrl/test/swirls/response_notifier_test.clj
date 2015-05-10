(ns yswrl.test.swirls.response-notifier-test
  (:use clojure.test)
  (:require [yswrl.swirls.response-notifier :as rn]))

(def some-swirl {:id 1 :title "This <great> thing"})
(def some-response {:swirl_id 1 :responder 2 :summary "<Sucked>"})
(def some-user {:id 2 :username "Johndon"})

(deftest response-notifier-test
  (testing "an HTML template is created with links to the swirl"
    (let [html (rn/response-notification-email-html some-swirl some-response some-user)]
      (is (.contains html (str "<a href=\"http://www.youshouldwatchreadlisten.com/swirls/1\">This &lt;great&gt; thing</a>")) html)
      (is (.contains html (str "<h2>&lt;Sucked&gt;</h2>")) html))))


