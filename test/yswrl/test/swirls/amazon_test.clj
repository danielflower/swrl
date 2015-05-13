(ns yswrl.test.swirls.amazon-test
  (:use clojure.test)
  (:require [yswrl.swirls.creation :as amazon])
  (:import (javax.crypto Mac)))

(deftest url-to-call
  (testing "url Params are in order"
    (let [urlToCall (amazon/url-to-call "javainterviewbootcamp")]
      (is (.contains urlToCall (str "http://webservices.amazon.com/onca/xml?AWSAccessKeyId=AKIAIO3J752UN7X4HUWA&AssociateTag=corejavaint0d-20&Keywords=javainterviewbootcamp&Operation=ItemSearch&ResponseGroup=Images%2CItemAttributes&SearchIndex=Books&Service=AWSECommerceService&")) urlToCall))))

(deftest encryption-check
  (testing "Amazon URL is correctly generated"
    (let [urlToCall (amazon/createEncryptedUrl (sorted-map
                                                 :AWSAccessKeyId "AKIAIO3J752UN7X4HUWA"
                                                 :AssociateTag "corejavaint0d-20"
                                                 :Keywords  "horcrux"
                                                 :Operation "ItemSearch"
                                                 :ResponseGroup "Images,ItemAttributes"
                                                 :SearchIndex "Books"
                                                 :Service "AWSECommerceService"
                                                 :Timestamp "2015-05-13T14:43:15.000Z"
                                                 :Version "2011-08-01"
                                                 ))]
      (is
        (.equals urlToCall
                 (str "http://webservices.amazon.com/onca/xml?AWSAccessKeyId=AKIAIO3J752UN7X4HUWA&AssociateTag=corejavaint0d-20&Keywords=horcrux&Operation=ItemSearch&ResponseGroup=Images%2CItemAttributes&SearchIndex=Books&Service=AWSECommerceService&Timestamp=2015-05-13T14%3A43%3A15.000Z&Version=2011-08-01&Signature=xfZmmyGu1ZpvpKtTfhs06Iz6Z2IYsdesMLMeEx14RA8%3D")) urlToCall))))


(deftest only-encryption-check
  (testing "Example 1 from http://docs.aws.amazon.com/AWSECommerceService/latest/DG/rest-signature.html"
    (let [urlToCall
          (amazon/toHexString
            (amazon/sign "1234567890"
                         "GET\nwebservices.amazon.com\n/onca/xml\nAWSAccessKeyId=AKIAIOSFODNN7EXAMPLE&AssociateTag=mytag-20&ItemId=0679722769&Operation=ItemLookup&ResponseGroup=Images%2CItemAttributes%2COffers%2CReviews&Service=AWSECommerceService&Timestamp=2014-08-18T12%3A00%3A00Z&Version=2013-08-01"))]
      (is
        (.equals urlToCall
                 (str "hxfZmmyGu1ZpvpKtTfhs06Iz6Z2IYsdesMLMeEx14RA8%3D")) urlToCall))))
