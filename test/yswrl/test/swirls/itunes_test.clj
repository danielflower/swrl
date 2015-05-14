(ns yswrl.test.swirls.itunes-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.itunes :as itunes])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker))


(deftest itunes-test


  (testing "Album titles, track listings, and thumbnail URLs can be extracted from itunes searches"
    (with-faked-responses
      (is (= {:results [
                        {:type "Album" :title "Mellon Collie and the Infinite Sadness (Deluxe Edition)" :artist "Smashing Pumpkins"
                         :itunes-id 721291853 :thumbnail-url "http://is2.mzstatic.com/image/pf/us/r30/Music4/v4/cc/13/f1/cc13f183-1cfb-4880-23a1-859f9c938ac6/05099997854159.60x60-50.jpg"}

                        {:type "Album" :title "Mellon Collie and the Infinite Sadness (Remastered)" :artist "Smashing Pumpkins"
                         :itunes-id 721224313 :thumbnail-url "http://is3.mzstatic.com/image/pf/us/r30/Music/v4/1b/82/d7/1b82d7ab-f019-e3bc-2f9d-45ea878d1cef/05099997856559.60x60-50.jpg"}
                        ]
              } (itunes/search-albums "Mellon Collie")))))

  (testing "Empty searchs retun empty arrays"
    (is (= {:results []} (itunes/search-albums nil)))
    (is (= {:results []} (itunes/search-albums "")))
    (is (= {:results []} (itunes/search-albums " "))))

  ;(testing "The iTunes REST API can be called"
  ;  (with-fake-routes
  ;    {""
  ;     (let [results (creation/search-albums "Mellon Collie")]
  ;       (is (> (results :resultCount) 0))
  ;       (is (= "Smashing Pumpkins" (:artistName (first (results :results))))))
  ;     }))

  ;(testing "An album can be retrieved from iTunes"
  ;  (with-faked-responses
  ;    {"https://itunes.apple.com/lookup?id=721224313&entity=song"
  ;     (fn [_] mellon-collie-and-the-infinite-sadness)}
  ;    (let [album (creation/get-itunes-album 721224313)]
  ;      (is (= "Mellon Collie and the Infinite Sadness (Remastered)" (album :title)))
  ;      (is (= "Smashing Pumpkins" (album :artist-name)))
  ;      (is (= 28 (count (album :tracks))))
  ;      (is (= "Tonight, Tonight" (:track-name (nth (album :tracks) 1)))))
  ;
  ;    ))
  )
