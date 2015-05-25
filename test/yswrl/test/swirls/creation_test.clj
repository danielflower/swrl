(ns yswrl.test.swirls.creation-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.creation :as c])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker)
  (:import java.net.URL))


(deftest creation-test

  (testing "YouTube IDs can be extracted from Youtube URLs"
    (is (= "TllPrdbZ-VI" (c/youtube-id "https://www.youtube.com/watch?v=TllPrdbZ-VI")))
    (is (= "TllPrdbZ-VI" (c/youtube-id "https://www.youtube.com/watch?v=TllPrdbZ-VI&whatever")))
    (is (= "TllPrdbZ-VI" (c/youtube-id "https://www.youtube.com/watch?v=TllPrdbZ-VI#blah")))
    (is (= "TllPrdbZ-VI" (c/youtube-id "http://m.youtube.com/watch?v=TllPrdbZ-VI"))))

  (testing "Video titles, embed HTML, and thumbnail URLs can be extracted from youtube requests"
    (with-faked-responses
      (is (= {:title         "How to chop an ONION using CRYSTALS with Jamie Oliver",
              :thumbnail-url "https://i.ytimg.com/vi/TllPrdbZ-VI/default.jpg",
              :iframe-html   "<iframe type=\"text/html\" src=\"http://www.youtube.com/embed/TllPrdbZ-VI\" width=\"640\" height=\"360\" frameborder=\"0\" allowfullscreen=\"true\"></iframe>",
              :review        "<p>Check this out:</p><iframe type=\"text/html\" src=\"http://www.youtube.com/embed/TllPrdbZ-VI\" width=\"640\" height=\"360\" frameborder=\"0\" allowfullscreen=\"true\"></iframe><p>What do you think?</p>"
              } (c/get-video-details "TllPrdbZ-VI")))))

  (testing "The Youtube handler is provided for YouTube URLs"
    (is (= c/handle-youtube-creation (c/handler-for (URL. "https://www.youtube.com/watch?v=7Hfz6A9ExPU&list=RD7Hfz6A9ExPU"))))
    (is (= c/handle-youtube-creation (c/handler-for (URL. "https://m.youtube.com/watch?v=7Hfz6A9ExPU&list=RD7Hfz6A9ExPU"))))
    (is (= c/handle-youtube-creation (c/handler-for (URL. "http://www.youtube.com/watch?v=7Hfz6A9ExPU"))))
    (is (= c/handle-youtube-creation (c/handler-for (URL. "http://youtube.com/watch?v=7Hfz6A9ExPU"))))
    (is (= c/handle-youtube-creation (c/handler-for (URL. "http://m.youtube.com/watch?v=7Hfz6A9ExPU&list=RD7Hfz6A9ExPU#wat")))))

  (testing "iTunes handler for iTunes"
    (is (= c/handle-itunes-creation (c/handler-for (URL. "https://itunes.apple.com/us/album/am/id663097964")))))


    (testing "iTunes ID extraction"
    (is (= "663097964" (c/itunes-id-from-url "https://itunes.apple.com/us/album/am/id663097964"))))

  (testing "All Amazon.com links go to the amazon creation handler"
    (is (= c/handle-amazon-creation (c/handler-for (URL. "http://www.amazon.com/Shogun-James-Clavell/dp/0440178002/ref=sr_1_1?ie=UTF8&qid=1432370486&sr=8-1&keywords=shogun")))))

  (testing "All tmdb links go to the tmdb creation handler"
    (is (= c/handle-tmdb-creation (c/handler-for (URL. "https://www.themoviedb.org/movie/401-garden-state")))))

  (testing "All other URLs use a generic website generation handler"
    (is (= c/handle-website-creation (c/handler-for (URL. "https://notyoutube.com/watch?v=blash")))))

  (testing "Asin extraction"
    (is (= "0440178002" (c/asin-from-url "http://www.amazon.com/Shogun-James-Clavell/dp/0440178002/ref=sr_1_1?ie=UTF8&qid=1432370486&sr=8-1&keywords=shogun")))
    (is (= "B0015T963C" (c/asin-from-url "http://www.amazon.com/Kindle-Wireless-Reading-Display-Generation/dp/B0015T963C")))
    (is (= "B0015T963C" (c/asin-from-url "http://www.amazon.com/dp/B0015T963C")))
    (is (= "B0015T963C" (c/asin-from-url "http://www.amazon.com/gp/product/B0015T963C")))
    (is (= "B0015T963C" (c/asin-from-url "http://www.amazon.com/gp/product/glance/B0015T963C")))
    )

  (testing "Asin extraction is nil if not found"
    (is (nil? (c/asin-from-url "http://www.amazon.com/Shogun-James-Clavell/dp/ef=sr_1_1?ie=UTF8&qid=1432370486&sr=8-1&keywords=shogun"))))

  (testing "Can generate correct IMDB URL from imdb-id"
    (is (= "http://www.imdb.com/title/tt0333766"
        (c/imdb-url "tt0333766"))))

  (testing "can get tmdb-id from TMDB URL"
    (is (= "401"
           (c/tmdb-id-from-url "https://www.themoviedb.org/movie/401-garden-state"))))

  )