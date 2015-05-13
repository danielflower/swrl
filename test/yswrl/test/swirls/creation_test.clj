(ns yswrl.test.swirls.creation-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.creation :as creation]
            [yswrl.db :as db]
            [yswrl.test.swirls.album-lookup-result :refer :all])
  (:use clojure.test)
  (:use clj-http.fake))

(deftest creation-test

  ;(testing "The iTunes REST API can be called"
  ;  (with-fake-routes
  ;    {""
  ;     (let [results (creation/search-albums "Mellon Collie")]
  ;       (is (> (results :resultCount) 0))
  ;       (is (= "Smashing Pumpkins" (:artistName (first (results :results))))))
  ;     }))

  (testing "An album can be retrieved from iTunes"
    (with-fake-routes
      {"https://itunes.apple.com/lookup?id=721224313&entity=song"
       (fn [_] mellon-collie-and-the-infinite-sadness)}
      (let [album (creation/get-itunes-album 721224313)]
        (is (= "Mellon Collie and the Infinite Sadness (Remastered)" (album :title)))
        (is (= "Smashing Pumpkins" (album :artist-name)))
        (is (= 28 (count (album :tracks))))
        (is (= "Tonight, Tonight" (:track-name (nth (album :tracks) 1)))))

      )))
