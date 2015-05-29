(ns yswrl.test.swirls.creation-test
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.creation :as c])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker)
  (:import java.net.URL))


(deftest creation-test

  (testing "iTunes handler for iTunes"
    (is (= c/handle-itunes-creation (c/handler-for (URL. "https://itunes.apple.com/us/album/am/id663097964")))))


  (testing "iTunes ID extraction"
    (is (= "663097964" (c/itunes-id-from-url "https://itunes.apple.com/us/album/am/id663097964"))))

  (testing "All Amazon.com links go to the amazon creation handler"
    (is (= c/handle-amazon-creation (c/handler-for (URL. "http://www.amazon.com/Shogun-James-Clavell/dp/0440178002/ref=sr_1_1?ie=UTF8&qid=1432370486&sr=8-1&keywords=shogun")))))

  (testing "All tmdb links go to the tmdb creation handler"
    (is (= c/handle-tmdb-creation (c/handler-for (URL. "https://www.themoviedb.org/movie/401-garden-state")))))

  (testing "All imdb links go to the imdb creation handler"
    (is (= c/handle-imdb-creation (c/handler-for (URL. "http://www.imdb.com/title/tt0333766/")))))

  (testing "All other URLs use a generic website generation handler"
    (is (= c/handle-website-creation (c/handler-for (URL. "https://youtube.com/watch?v=blash")))))

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

  (testing "can get imdb-id from IMDB URL"
    (is (= "tt0333766" (c/imdb-id-from-url "http://www.imdb.com/title/tt0333766")))
    (is (= "tt0333766" (c/imdb-id-from-url "http://www.imdb.com/title/tt0333766/")))
    (is (= "tt0333766" (c/imdb-id-from-url "http://www.imdb.com/title/tt0333766?some_key=value")))
    (is (= "tt0333766" (c/imdb-id-from-url "http://www.imdb.com/title/tt0333766#_=_"))))

  )