(ns yswrl.test.links-test
  (:use clojure.test)
  (:require [yswrl.links :as links]))

(deftest links
  (testing "swirl URL creation"
    (is (= "/swirls/10" (links/swirl 10))))

  (testing "password reset link"
    (is (= "/reset-password?token=ADSF%25%26" (links/password-reset "ADSF%&"))))

  (testing "user links"
    (is (= "/swirls/by/Dan+%26+%2F+co" (links/user "Dan & / co"))))

  (testing "absolute URLs"
    (is (= "http://www.swrl.co/swirls/10" (links/absolute "/swirls/10"))))


  (testing "itunes album"
    (is (= "https://itunes.apple.com/us/album/id721224313?at=" (links/itunes-album 721224313))))

  (testing "gravatar URLs include the hash"
    (is (= "http://www.gravatar.com/avatar/0bc83cb571cd1c50ba6f3e8a78ef1346?s=40&d=monsterid"
           (links/gravatar-url "0bc83cb571cd1c50ba6f3e8a78ef1346" 40)))
    )

  )
