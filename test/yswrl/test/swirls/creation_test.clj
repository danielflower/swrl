(ns yswrl.test.swirls.creation-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.creation :as creation])
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
              :iframe-html   "<iframe type=\"text/html\" src=\"http://www.youtube.com/embed/TllPrdbZ-VI\" width=\"640\" height=\"360\" frameborder=\"0\" allowfullscreen=\"true\"></iframe>",
              :review        "<p>Check this out:</p><iframe type=\"text/html\" src=\"http://www.youtube.com/embed/TllPrdbZ-VI\" width=\"640\" height=\"360\" frameborder=\"0\" allowfullscreen=\"true\"></iframe><p>What do you think?</p>"
              } (creation/get-video-details "TllPrdbZ-VI")))))

  )
