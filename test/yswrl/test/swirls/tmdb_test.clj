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
                         :create-url "/create/movie?tmdb-id=401"
                         :thumbnail-url "http://image.tmdb.org/t/p/original/dyuQeCN6K4DaUnm6RjenCBPI5nb.jpg"
                         :large-image-url "http://image.tmdb.org/t/p/original/dyuQeCN6K4DaUnm6RjenCBPI5nb.jpg"}

                        {:title "The Marshall Tucker Band - Live From The Garden State 1981"
                         :tmdb-id 324409
                         :create-url "/create/movie?tmdb-id=324409"
                         :thumbnail-url "http://image.tmdb.org/t/p/original/gQU9pdJ4rGN8GvxaL2auvJ5mmXw.jpg"
                         :large-image-url "http://image.tmdb.org/t/p/original/gQU9pdJ4rGN8GvxaL2auvJ5mmXw.jpg"}

                        ]}
             (tmdb/search-movies "garden state")))))

  (testing "TV info can be extracted from tmdb search"
    (with-faked-responses
      (is (= {:results [
                        {:title "Black Mirror"
                         :tmdb-id 42009
                         :create-url "/create/tv?tmdb-id=42009"
                         :thumbnail-url "http://image.tmdb.org/t/p/original/yffVc4I6OYSv5JWeXPQPnHyvMDy.jpg"
                         :large-image-url "http://image.tmdb.org/t/p/original/yffVc4I6OYSv5JWeXPQPnHyvMDy.jpg"}
                        ]}
             (tmdb/search-tv "black mirror")))))

  (testing "Empty movie searches retun empty arrays"
    (is (= {:results []} (tmdb/search-movies nil)))
    (is (= {:results []} (tmdb/search-movies "")))
    (is (= {:results []} (tmdb/search-movies " "))))

  (testing "Empty tv searches retun empty arrays"
    (is (= {:results []} (tmdb/search-tv nil)))
    (is (= {:results []} (tmdb/search-tv "")))
    (is (= {:results []} (tmdb/search-tv " "))))

  (testing "Can get the movie details from a tmdb ID"
    (with-faked-responses
      (is (= {:title           "Garden State"
           :tmdb-id         401
           :thumbnail-url   "http://image.tmdb.org/t/p/original/dyuQeCN6K4DaUnm6RjenCBPI5nb.jpg"
           :large-image-url "http://image.tmdb.org/t/p/original/dyuQeCN6K4DaUnm6RjenCBPI5nb.jpg"
           :url "http://www2.foxsearchlight.com/gardenstate/"
           :overview "Andrew returns to his hometown for the funeral of his mother, a journey that reconnects him with past friends. The trip coincides with his decision to stop taking his powerful antidepressants. A chance meeting with Sam - a girl also suffering from various maladies - opens up the possibility of rekindling emotional attachments, confronting his psychologist father, and perhaps beginning a new life."
           :imdb-id "tt0333766"
           :tagline "None"
           :genres [{:genre "Comedy"}
                    {:genre "Drama"}
                    {:genre "Romance"}]}
             (tmdb/get-movie-from-tmdb-id 401)))))

  (testing "Can get the tv details from a tmdb ID"
    (with-faked-responses
      (is (= {:title           "Black Mirror"
              :tmdb-id         42009
              :thumbnail-url   "http://image.tmdb.org/t/p/original/yffVc4I6OYSv5JWeXPQPnHyvMDy.jpg"
              :large-image-url "http://image.tmdb.org/t/p/original/yffVc4I6OYSv5JWeXPQPnHyvMDy.jpg"
              :url "http://www.channel4.com/programmes/black-mirror/"
              ;;:imdb-id "tt2085059" ;; API doesn't provide this yet, sadface
              }
             (tmdb/get-tv-from-tmdb-id 42009)))
    )
  )

  (testing "Can get the tmdb id from an IMDB ID"
    (with-faked-responses
      (is (= {:tmdb-id 401
              :type "movie"}
            (tmdb/get-tmdb-id-from-imdb-id "tt0333766"))))

    (with-faked-responses
      (is (= {:tmdb-id 42009
              :type "tv"}
             (tmdb/get-tmdb-id-from-imdb-id "tt2085059")))))

  (testing "tmdb id is nil if bad IMDB ID is given"
    (with-faked-responses
      (is (= nil
             (tmdb/get-tmdb-id-from-imdb-id "nope")))))


  )