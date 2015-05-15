(ns yswrl.swirls.amazon
  (:use [clojure.data.zip.xml :only (attr text xml-> xml1->)])
  (:require [yswrl.links :as links]
            [clj-http.client :as client]
            [clj-time.core :as t]
            [buddy.core.codecs :as codecs]
            [buddy.core.mac.hmac :as hmac]
            [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.data.xml :as xml-data]))


(def amazon-key "tDwOEE+vRplK8EhmRhrt8BIuxZi1NvSYbmpwxTv5")

(def params (sorted-map
              :AWSAccessKeyId "AKIAIO3J752UN7X4HUWA"
              :AssociateTag "corejavaint0d-20"
              :ResponseGroup "Images,ItemAttributes,EditorialReview"
              :Service "AWSECommerceService"
              :Timestamp (str (t/now))
              :Version "2011-08-01"
              ))

(defn string-to-sign [pms]
  (str "GET\nwebservices.amazon.com\n/onca/xml\n"
       (ring.util.codec/form-encode pms)))

(defn sign [key string]
  (-> (hmac/hash string key :sha256)
      (codecs/bytes->base64)))


(defn createEncryptedUrl [paz]
  (let [to-sign (string-to-sign paz)
        signed (sign amazon-key to-sign)
        encodedSignature (ring.util.codec/form-encode signed)]
    (str "http://webservices.amazon.com/onca/xml?" (ring.util.codec/form-encode paz) "&Signature=" encodedSignature)))

(defn search-url [bookname]
  (createEncryptedUrl (assoc params :Keywords bookname :Operation "ItemSearch" :SearchIndex "Books"
                                    )))

(defn handle-amazon [bookname]
  (let [url (search-url bookname)
        raw-data ((client/get url) :body)
        result-data (xml-data/parse-str raw-data)]
    (zip/xml-zip result-data)))

(defn search-books [search-term]
  (if (clojure.string/blank? search-term)
    {:results []}
    (let [result (handle-amazon (ring.util.codec/url-encode search-term))] {
                                                                            :results (map (fn [r] {:url             (apply str (xml-> r :DetailPageURL text))
                                                                                                   :title           (apply str (xml-> r :ItemAttributes :Title text))
                                                                                                   :author          (apply str (xml-> r :ItemAttributes :Author text))
                                                                                                   :book-id         (apply str (xml-> r :ASIN text))
                                                                                                   :thumbnail-url   (apply str (xml-> r :SmallImage :URL text))
                                                                                                   :large-image-url (apply str (xml-> r :LargeImage :URL text))}) (xml-> result :Items :Item))
                                                                            })))


(defn item-url [item-id]
  (createEncryptedUrl (assoc params :idType "ASIN" :ItemId item-id :Operation "ItemLookup")))

(defn get-book [asin]
  (let [url (item-url asin)
        raw-data ((client/get url) :body)
        result-data (xml-data/parse-str raw-data)
        zip-data (zip/xml-zip result-data)
        book (xml1-> zip-data :Items :Item)]
    (println url)
    {:title       (apply str (xml-> book :ItemAttributes :Title text))
     :author      (apply str (xml-> book :ItemAttributes :Author text))
     :big-img-url (apply str (xml-> book :LargeImage :URL text))
     :blurb       (apply str (xml-> book :EditorialReviews :EditorialReview :Content text))
     }
    )

  )

