(ns yswrl.test.swirls.amazon-test
  (:use clojure.test)
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.amazon :as amazon])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))

(deftest amazon-test

  (testing "url Params are in order"
    (let [urlToCall (amazon/search-url "javainterviewbootcamp")]
      (is (.contains urlToCall (str "https://webservices.amazon.com/onca/xml?AWSAccessKeyId=AKIAIO3J752UN7X4HUWA&AssociateTag=corejavaint0d-20&Keywords=javainterviewbootcamp&Operation=ItemSearch&ResponseGroup=Images%2CItemAttributes%2CEditorialReview&SearchIndex=Books&Service=AWSECommerceService&")) urlToCall)))

  (testing "Amazon URL is correctly generated"
    (let [urlToCall (amazon/createEncryptedUrl (sorted-map
                                                 :AWSAccessKeyId "AKIAIO3J752UN7X4HUWA"
                                                 :AssociateTag "corejavaint0d-20"
                                                 :Keywords "horcrux"
                                                 :Operation "ItemSearch"
                                                 :ResponseGroup "Images,ItemAttributes"
                                                 :SearchIndex "Books"
                                                 :Service "AWSECommerceService"
                                                 :Timestamp "2015-05-14T04:26:12.000Z"
                                                 :Version "2011-08-01"
                                                 ))]
      (is
        (.equals urlToCall
                 (str "https://webservices.amazon.com/onca/xml?AWSAccessKeyId=AKIAIO3J752UN7X4HUWA&AssociateTag=corejavaint0d-20&Keywords=horcrux&Operation=ItemSearch&ResponseGroup=Images%2CItemAttributes&SearchIndex=Books&Service=AWSECommerceService&Timestamp=2015-05-14T04%3A26%3A12.000Z&Version=2011-08-01&Signature=NpX9t1jcz6wR2CCLKfG%2B8Pt2RxQli9QbAZu3dHpifXc%3D")) urlToCall)))


  (testing "Example 1 from http://docs.aws.amazon.com/AWSECommerceService/latest/DG/rest-signature.html"
    (let [urlToCall
          (ring.util.codec/form-encode (amazon/sign "1234567890"
                                                    "GET\nwebservices.amazon.com\n/onca/xml\nAWSAccessKeyId=AKIAIOSFODNN7EXAMPLE&AssociateTag=mytag-20&ItemId=0679722769&Operation=ItemLookup&ResponseGroup=Images%2CItemAttributes%2COffers%2CReviews&Service=AWSECommerceService&Timestamp=2014-08-18T12%3A00%3A00Z&Version=2013-08-01"))]
      (is
        (.equals urlToCall
                 (str "j7bZM0LXZ9eXeZruTqWm2DIvDYVUU3wxPPpp%2BiXxzQc%3D")) urlToCall)))

  (testing "Real example using my key generated from http://associates-amazon.s3.amazonaws.com/scratchpad/index.html"
    (let [urlToCall
          (ring.util.codec/form-encode (amazon/sign amazon/amazon-key
                                                    (amazon/string-to-sign (sorted-map
                                                                             :AWSAccessKeyId "AKIAIO3J752UN7X4HUWA"
                                                                             :AssociateTag "corejavaint0d-20"
                                                                             :Keywords "horcrux"
                                                                             :Operation "ItemSearch"
                                                                             :ResponseGroup "Images,ItemAttributes"
                                                                             :SearchIndex "Books"
                                                                             :Service "AWSECommerceService"
                                                                             :Timestamp "2015-05-14T04:26:12.000Z"
                                                                             :Version "2011-08-01"
                                                                             ))))]
      (is
        (.equals urlToCall
                 (str "NpX9t1jcz6wR2CCLKfG%2B8Pt2RxQli9QbAZu3dHpifXc%3D")) urlToCall)))

  #_(testing "Book info can be extracted from Amazon Search"
    (with-faked-responses
      (is (= {:results [
                        {:url             "http://www.amazon.com/The-Hunger-Games-Book-1/dp/0439023521%3FSubscriptionId%3DAKIAIO3J752UN7X4HUWA%26tag%3Dcorejavaint0d-20%26linkCode%3Dxm2%26camp%3D2025%26creative%3D165953%26creativeASIN%3D0439023521"
                         :title           "The Hunger Games (Book 1)"
                         :author          "Suzanne Collins"
                         :large-image-url "http://ecx.images-amazon.com/images/I/41bOj-am1RL.jpg"
                         :thumbnail-url   "http://ecx.images-amazon.com/images/I/41bOj-am1RL._SL75_.jpg"}

                        {:url             "http://www.amazon.com/Hunger-Games-Trilogy-Catching-Mockingjay/dp/B0075N4OK2%3FSubscriptionId%3DAKIAIO3J752UN7X4HUWA%26tag%3Dcorejavaint0d-20%26linkCode%3Dxm2%26camp%3D2025%26creative%3D165953%26creativeASIN%3DB0075N4OK2"
                         :title           "The Hunger Games Trilogy (The Hunger Games / Catching Fire / Mockingjay)"
                         :author          "Suzanne Collins"
                         :large-image-url "http://ecx.images-amazon.com/images/I/31XWc6gbQLL.jpg"
                         :thumbnail-url   "http://ecx.images-amazon.com/images/I/31XWc6gbQLL._SL30_.jpg"}]}
             (amazon/search-books "the hunger games")))))

#_(testing "Game info can be extracted from Amazon Search"
    (with-faked-responses
      (is (= {:results [
                        {:url             "http://www.amazon.com/The-Hunger-Games-Book-1/dp/0439023521%3FSubscriptionId%3DAKIAIO3J752UN7X4HUWA%26tag%3Dcorejavaint0d-20%26linkCode%3Dxm2%26camp%3D2025%26creative%3D165953%26creativeASIN%3D0439023521"
                         :title           "The Hunger Games (Book 1)"
                         :author          "Suzanne Collins"
                         :large-image-url "http://ecx.images-amazon.com/images/I/41bOj-am1RL.jpg"
                         :thumbnail-url   "http://ecx.images-amazon.com/images/I/41bOj-am1RL._SL75_.jpg"}

                        {:url             "http://www.amazon.com/Hunger-Games-Trilogy-Catching-Mockingjay/dp/B0075N4OK2%3FSubscriptionId%3DAKIAIO3J752UN7X4HUWA%26tag%3Dcorejavaint0d-20%26linkCode%3Dxm2%26camp%3D2025%26creative%3D165953%26creativeASIN%3DB0075N4OK2"
                         :title           "The Hunger Games Trilogy (The Hunger Games / Catching Fire / Mockingjay)"
                         :author          "Suzanne Collins"
                         :large-image-url "http://ecx.images-amazon.com/images/I/31XWc6gbQLL.jpg"
                         :thumbnail-url   "http://ecx.images-amazon.com/images/I/31XWc6gbQLL._SL30_.jpg"}]}
             (amazon/search-games "the last of us"))))) )
