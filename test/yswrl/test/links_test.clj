(ns yswrl.test.links-test
  (:use clojure.test)
  (:require [yswrl.links :as links]))

(deftest links
  (testing "swirl URL creation"
    (is (= "/swirls/10" (links/swirl 10))))

  (testing "swirl URL creation with comments"
    (is (= "/swirls/10#1456"
           (links/swirl 10 1456))))

  (testing "password reset link"
    (is (= "/reset-password?token=ADSF%25%26" (links/password-reset "ADSF%&"))))

  (testing "user links"
    (is (= "/profile/Dan%20%26%20%2F%20co" (links/user "Dan & / co"))))

  (testing "absolute URLs"
    (is (= "http://www.swrl.co/swirls/10" (links/absolute "/swirls/10"))))

  (testing "inbox by response"
    (is (= "/swirls/inbox/hot" (links/inbox "hot")))
    (is (= "/swirls/inbox/loved%20it" (links/inbox "Loved It"))))

  (testing "itunes album"
    (is (= "https://itunes.apple.com/us/album/id721224313?at=1001l55M" (links/itunes-album 721224313))))

  (testing "gravatar URLs include the hash"
    (is (= "http://www.gravatar.com/avatar/0bc83cb571cd1c50ba6f3e8a78ef1346?s=40&d=monsterid&r=pg"
           (links/gravatar-url "0bc83cb571cd1c50ba6f3e8a78ef1346" 40)))
    )

  (testing "edit swirl link has query strings added"
    (is (= "/swirls/123/edit?origin-swirl-id=12&private=true"
           (links/edit-swirl 123 "origin-swirl-id=12&private=true"))))

  (testing "group join"
    (is (= "/groups/123/join/1234123412342" (links/join-group 123 1234123412342)))
    (is (= (links/join-group 123 1234123412342) (links/join-group {:id 123 :join_code 1234123412342}))))

  (testing "edit swirl link has no params if query-string id is nil"
    (is (= "/swirls/123/edit"
           (links/edit-swirl 123 nil)))
    (is (= "/swirls/123/edit"
           (links/edit-swirl 123))))
  )
