(ns yswrl.test.swirls.tmdb-test
  (:use clojure.test)
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.tmdb :as tmdb])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))

(deftest tmdb-test

  (testing "Movie info can be extracted from tmdb search"
    (with-faked-responses
      (is (= {:results [
                        {:title "Garden State"
                         :tmdb-id 401
                         :thumbnail-url "http://image.tmdb.org/t/p/w92/u7IASCZ02Q94SYklSIR2609inis.jpg"
                         :large-image-url "http://image.tmdb.org/t/p/w342/u7IASCZ02Q94SYklSIR2609inis.jpg"}

                        {:title "The Marshall Tucker Band - Live From The Garden State 1981"
                         :tmdb-id 324409
                         :thumbnail-url "http://image.tmdb.org/t/p/w92/gQU9pdJ4rGN8GvxaL2auvJ5mmXw.jpg"
                         :large-image-url "http://image.tmdb.org/t/p/w342/gQU9pdJ4rGN8GvxaL2auvJ5mmXw.jpg"}

                        ]}
             (tmdb/search-movies "garden state")))))

  (testing "Empty searchs retun empty arrays"
    (is (= {:results []} (tmdb/search-movies nil)))
    (is (= {:results []} (tmdb/search-movies "")))
    (is (= {:results []} (tmdb/search-movies " "))))

  (testing "Can get the movie details from a tmdb ID"
    (with-faked-responses
      (is (= {:title           "Garden State"
           :tmdb-id         401
           :thumbnail-url   "http://image.tmdb.org/t/p/w92/u7IASCZ02Q94SYklSIR2609inis.jpg"
           :large-image-url "http://image.tmdb.org/t/p/w342/u7IASCZ02Q94SYklSIR2609inis.jpg"
           :url "http://www2.foxsearchlight.com/gardenstate/"
           :overview "Andrew returns to his hometown for the funeral of his mother, a journey that reconnects him with past friends. The trip coincides with his decision to stop taking his powerful antidepressants. A chance meeting with Sam - a girl also suffering from various maladies - opens up the possibility of rekindling emotional attachments, confronting his psychologist father, and perhaps beginning a new life."
           :imdb-id "tt0333766"
           :tagline "None"
           :genres [{:genre "Comedy"}
                    {:genre "Drama"}
                    {:genre "Romance"}]}
             (tmdb/get-movie-from-tmdb-id 401)))))

  (testing "Can get the tmdb id from an IMDB ID"
    (with-faked-responses
      (is (= 401
            (tmdb/get-tmdb-id-from-imdb-id "tt0333766")))))

  )