(ns yswrl.test.swirls.creation-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.creation :as creation]
            [yswrl.test.swirls.album-lookup-result :refer :all])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker))


(deftest creation-test

  (testing "YouTube IDs can be extracted from Youtube URLs"
    (is (= "TllPrdbZ-VI" (creation/youtube-id "https://www.youtube.com/watch?v=TllPrdbZ-VI")))
    (is (= "TllPrdbZ-VI" (creation/youtube-id "https://www.youtube.com/watch?v=TllPrdbZ-VI&whatever")))
    (is (= "TllPrdbZ-VI" (creation/youtube-id "https://www.youtube.com/watch?v=TllPrdbZ-VI#blah")))
    (is (= "TllPrdbZ-VI" (creation/youtube-id "http://m.youtube.com/watch?v=TllPrdbZ-VI"))))

  (testing "Video titles, embed HTML, and thumbnail URLs can be extracted from youtube requests"
    (with-faked-responses
      (is (= {:title         "How to chop an ONION using CRYSTALS with Jamie Oliver",
              :thumbnail-url "https://i.ytimg.com/vi/TllPrdbZ-VI/default.jpg",
              :iframe-html   "<iframe type='text/html' src='http://www.youtube.com/embed/TllPrdbZ-VI' width='640' height='360' frameborder='0' allowfullscreen='true'/>",
              :review        "<p>Check this out:</p><iframe type='text/html' src='http://www.youtube.com/embed/TllPrdbZ-VI' width='640' height='360' frameborder='0' allowfullscreen='true'/><p>What do you think?</p>"
              } (creation/get-video-details "TllPrdbZ-VI")))))

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
