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
                        {:title           "Garden State (2004)"
                         :tmdb-id         401
                         :create-url      "/create/movie?tmdb-id=401&"
                         :overview        "Movie about Zach Braff being Zach Braff"
                         :thumbnail-url   "https://image.tmdb.org/t/p/original/u7IASCZ02Q94SYklSIR2609inis.jpg"
                         :large-image-url "https://image.tmdb.org/t/p/original/u7IASCZ02Q94SYklSIR2609inis.jpg"}

                        {:title           "The Marshall Tucker Band - Live From The Garden State 1981 (2004)"
                         :tmdb-id         324409
                         :create-url      "/create/movie?tmdb-id=324409&"
                         :overview        "no idea, some live music rubbish"
                         :thumbnail-url   "https://image.tmdb.org/t/p/original/gQU9pdJ4rGN8GvxaL2auvJ5mmXw.jpg"
                         :large-image-url "https://image.tmdb.org/t/p/original/gQU9pdJ4rGN8GvxaL2auvJ5mmXw.jpg"}

                        ]}
             (tmdb/search-movies "garden state")))))

  (testing "TV info can be extracted from tmdb search"
    (with-faked-responses
      (is (= {:results [
                        {:title           "Black Mirror"
                         :tmdb-id         42009
                         :create-url      "/create/tv?tmdb-id=42009&"
                         :thumbnail-url   "https://image.tmdb.org/t/p/original/djUxgzSIdfS5vNP2EHIBDIz9I8A.jpg"
                         :large-image-url "https://image.tmdb.org/t/p/original/djUxgzSIdfS5vNP2EHIBDIz9I8A.jpg"}
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
              :thumbnail-url   "https://image.tmdb.org/t/p/original/u7IASCZ02Q94SYklSIR2609inis.jpg"
              :large-image-url "https://image.tmdb.org/t/p/original/u7IASCZ02Q94SYklSIR2609inis.jpg"
              :url             "http://www2.foxsearchlight.com/gardenstate/"
              :overview        "A quietly troubled young man returns home for his mother's funeral after being estranged from his family for a decade."
              :imdb-id         "tt0333766"
              :tagline         "None"
              :release-year    "2004"
              :genres          ["Comedy"
                                "Drama"
                                "Romance"]
              :director        "Zach Braff"
              :ratings         [
                                {
                                 :Source "Internet Movie Database",
                                 :Value  "7.6/10"
                                 },
                                {
                                 :Source "Rotten Tomatoes",
                                 :Value  "86%"
                                 },
                                {
                                 :Source "Metacritic",
                                 :Value  "67/100"
                                 }
                                ]
              :runtime         "102 min"
              :actors          "Zach Braff, Kenneth Graymez, George C. Wolfe, Austin Lysy"}
             (tmdb/get-movie-from-tmdb-id 401)))))

  (testing "Can get the tv details from a tmdb ID"
    (with-faked-responses
      (is (= {:title           "Black Mirror"
              :tmdb-id         42009
              :creator         "Charlie Brooker, Another Creator"
              :overview        "Black Mirror is a British television drama series created by Charlie Brooker and shows the dark side of life and technology. The series is produced by Zeppotron for Endemol. Regarding the programme's content and structure, Brooker noted, \"each episode has a different cast, a different setting, even a different reality. But they're all about the way we live now – and the way we might be living in 10 minutes' time if we're clumsy.\"\n\nAn Endemol press release describes the series as \"a hybrid of The Twilight Zone and Tales of the Unexpected which taps into our contemporary unease about our modern world\", with the stories having a \"techno-paranoia\" feel. Channel 4 describes the first episode as \"a twisted parable for the Twitter age\". Black Mirror Series 1 was released on DVD on 27 February 2012.\n\nIn November 2012, Black Mirror won the Best TV movie/mini-series award at the International Emmys.\n\nAnnounced on 12 July 2012, the second series began broadcasting on 11 February 2013. Like the first series, it is made up of three episodes with unconnected narratives.\n\nRobert Downey, Jr. has optioned the episode The Entire History of You, to potentially be made into a film by Warner Bros. and his own production company Team Downey."
              :thumbnail-url   "https://image.tmdb.org/t/p/original/djUxgzSIdfS5vNP2EHIBDIz9I8A.jpg"
              :large-image-url "https://image.tmdb.org/t/p/original/djUxgzSIdfS5vNP2EHIBDIz9I8A.jpg"
              :url             "http://www.channel4.com/programmes/black-mirror/"
              ;;:imdb-id "tt2085059" ;; API doesn't provide this yet, sadface
              }
             (tmdb/get-tv-from-tmdb-id 42009)))
      )
    )

  (testing "Can get the tmdb id from an IMDB ID"
    (with-faked-responses
      (is (= {:tmdb-id 401
              :type    "movie"}
             (tmdb/get-tmdb-id-from-imdb-id "tt0333766"))))

    (with-faked-responses
      (is (= {:tmdb-id 42009
              :type    "tv"}
             (tmdb/get-tmdb-id-from-imdb-id "tt2085059")))))

  (testing "tmdb id is nil if bad IMDB ID is given"
    (with-faked-responses
      (is (= nil
             (tmdb/get-tmdb-id-from-imdb-id "nope")))))

  )