(ns yswrl.test.swirls.website-test
  (:use clojure.test)
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.website :as website]
            [clj-http.client :as client])
  (:use clj-http.fake)
  (:use yswrl.fake.faker)
  (:use midje.sweet))

(deftest website-test

  (testing "Can get metadata from website html"
    (with-faked-responses
      (is (= {:title "Archer (TV Series 2009– )"
              :site-name "IMDb"
              :image-url "http://ia.media-imdb.com/images/M/MV5BMTg3NTMwMzY2OF5BMl5BanBnXkFtZTgwMDcxMjQ0NDE@._V1_SY1080_CR45,0,630,1080_AL_.jpg"
              :description "Created by Adam Reed.  With H. Jon Benjamin, Judy Greer, Amber Nash, Chris Parnell. At ISIS, an international spy agency, global crises are merely opportunities for its highly trained employees to confuse, undermine, betray and royally screw each other."
              :type "website"
              :embed-html nil
              }
            (website/get-metadata "http://www.imdb.com/title/tt1486217/?ref_=nv_sr_1")))))

  (testing "Can get some metadata from website html without open graph tags"
    (with-faked-responses
      (is (= {:title "Progressive enhancement is still important - JakeArchibald.com"
              :site-name nil
              :image-url nil
              :description nil
              :embed-html nil
              :type "website"
              }
            (website/get-metadata "http://jakearchibald.com/2013/progressive-enhancement-still-important/")))))

  (testing "Can extract an embedded video from video pages"
    (with-faked-responses
      (is (= {:title "Auto Tuning"
              :site-name "Vimeo"
              :image-url "https://i.vimeocdn.com/video/5211842_1280x720.webp"
              :description "Vimeo HQ - 4:12pm  Blake needs to talk to Jack about the homepage... or at least he tries to."
              :embed-html "<iframe sandbox=\"allow-scripts allow-same-origin allow-popups\" width=\"640\" height=\"360\" src=\"https://player.vimeo.com/video/3718294?autoplay=0\" frameborder=\"0\" allowfullscreen></iframe>"
              :type "video"
              }
            (website/get-metadata "http://exact.match.com/vimeo.3718294.html")))))

  (testing "If video URLs are provided but they are flash then don't embed the video"
    (with-faked-responses
      (do
        (is (nil? (:embed-html (website/get-metadata "http://exact.match.com/xliue_knifin-around_fun.html"))))
        (is (nil? (:embed-html (website/get-metadata "http://exact.match.com/redbull-video.html")))))))

  (testing "Handles a junk url nicely"
    (with-faked-responses
      (is (= {:title nil
              :site-name nil
              :image-url nil
              :description nil
              :embed-html nil
              :type "website"
              }
            (website/get-metadata "http://www.fjljldjfjdsoifjsdf.com/")))))
  )

(def fake-html-resource-response
  '({:attrs nil, :content ({:attrs nil, :content ("..body.."), :tag :body}), :tag :html}))

(def fake-empty-html-resource-response
  '({:attrs nil, :content ({:attrs nil, :content nil, :tag :body}), :tag :html}))

(facts "about dealing with urls without protocols"
       (fact "if url works return resource"
             (website/fetch-url "..url.." ) => fake-html-resource-response
             (provided
               (client/get "..url..") => {:body "..body.."}))

       (fact "if url doesn't work try adding http and try again"
             (website/fetch-url "..url..") => fake-html-resource-response
             (provided
               (client/get "..url..") =throws=> (Exception.)
               (client/get "http://..url..") => {:body "..body.."}))

       (fact "if url doesn't work and http fails too, try https"
             (website/fetch-url "..url..") => fake-html-resource-response
             (provided
               (client/get "..url..") =throws=> (Exception.)
               (client/get "http://..url..") =throws=> (Exception.)
               (client/get "https://..url..") => {:body "..body.."}))

       (fact "if nothing works, just return empty response"
             (website/fetch-url "..url..") => fake-empty-html-resource-response
             (provided
               (client/get "..url..") =throws=> (Exception.)
               (client/get "http://..url..") =throws=> (Exception.)
               (client/get "https://..url..") =throws=> (Exception.))))