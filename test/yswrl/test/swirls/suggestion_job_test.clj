(ns yswrl.test.swirls.suggestion-job-test
  (:use clojure.test)
  (:require [yswrl.swirls.suggestion-job :as sug])
  (:import (java.util UUID)))

(deftest comment-notifier
  (testing "an HTML template is created with links to the swirl"
    (let [code (UUID/randomUUID)
          html (sug/suggestion-email-html {:swirl_id 1 :title "Some <great> thing" :author_name "F<rank>" :code code})]
      (is (.contains html (str "<a href=\"http://www.youshouldwatchreadlisten.com/swirls/1?code=" (str code) "\">Some &lt;great&gt; thing</a>")) html)
      (is (.contains html (str "F&lt;rank&gt; has recommended that you check out")) html))))


