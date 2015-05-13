(ns yswrl.test.swirls.amazon-test
  (:use clojure.test)
  (:require [yswrl.swirls.creation :as amazon]))

(deftest comment-notifier
  (testing "Amazon URL is correctly generated"
    (let [urlToCall (amazon/url-to-call "javainterviewbootcamp")]
      (is (.contains urlToCall (str "http://webservices.amazon.com/onca/xml?AWSAccessKeyId=AKIAIO3J752UN7X4HUWA&AssociateTag=corejavaint0d-20&Keywords=javainterviewbootcamp&Operation=ItemSearch&ResponseGroup=Images%2CItemAttributes&SearchIndex=Books&Service=AWSECommerceService&Timestamp=2015-05-10T01%3A38%3A58.000Z&Version=2011-08-01")) urlToCall))))
