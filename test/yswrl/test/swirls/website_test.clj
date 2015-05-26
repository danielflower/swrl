(ns yswrl.test.swirls.website-test
  (:use clojure.test)
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.website :as website])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))

(deftest website-test

  (testing "Can get metadata from website html"
    (with-faked-responses
      (is (= {:title "Archer (TV Series 2009– )"
              :site-name "IMDb"
              :image-url "http://ia.media-imdb.com/images/M/MV5BMTg3NTMwMzY2OF5BMl5BanBnXkFtZTgwMDcxMjQ0NDE@._V1_SY1080_CR45,0,630,1080_AL_.jpg"
              :description "Created by Adam Reed.  With H. Jon Benjamin, Judy Greer, Amber Nash, Chris Parnell. At ISIS, an international spy agency, global crises are merely opportunities for its highly trained employees to confuse, undermine, betray and royally screw each other."
              }
             (website/get-metadata "http://www.imdb.com/title/tt1486217/?ref_=nv_sr_1")))))

  (testing "Can get some metadata from website html without open graph tags"
    (with-faked-responses
      (is (= {:title "Progressive enhancement is still important - JakeArchibald.com"
              :site-name nil
              :image-url nil
              :description nil
              }
             (website/get-metadata "http://jakearchibald.com/2013/progressive-enhancement-still-important/")))))

  )
