(ns yswrl.test.swirls.swirl-routes-test
  (:require [yswrl.test.scaffolding :as s]
            [yswrl.swirls.swirl-routes :as routes]
            [yswrl.groups.groups-repo :as groups-repo]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.user.notifications-repo :as notifications-repo]
            [yswrl.user.notifications :as notifications])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker)
  (:use midje.sweet))

(deftest swirls-repo-test

  (let [author (s/create-test-user)
        recipient (s/create-test-user)
        group (groups-repo/create-group (author :id) "My Group" "This is a group for the ages")
        _ (groups-repo/add-group-member (group :id) (recipient :id))
        _ (groups-repo/add-group-member (group :id) (author :id))
        draft-swirl (repo/save-draft-swirl "website" (author :id) "Animals (draft)" "What to write...." nil)]

    (testing "Users can be added via the select box and via groups and they are notified only once"
      (routes/publish-swirl author (draft-swirl :id) ["somebody"] "The published swirl" "It is a great swirl" nil [(group :id)] false "movie" "http://fake" false)
      (is (= (map :notification_type (notifications-repo/get-for-user-page (recipient :id))) [notifications/recommendation]))
      (routes/publish-swirl author (draft-swirl :id) ["somebody"] "The published swirl" "It is a great swirl" nil [(group :id)] false "movie" "http://fake" false)
      (is (= (map :notification_type (notifications-repo/get-for-user-page (recipient :id))) [notifications/recommendation]))
      (is (empty? (notifications-repo/get-for-user-page (author :id))))
      (is (= [(recipient :id)] (map :user-id (repo/get-suggestion-usernames (draft-swirl :id)))))
      )))

(facts "about getting 'swirls' from search results"
       (fact "can convert search-results to a swirl map"
             (routes/convert-to-swirl-list {:results [
                                                      {:type       "Album" :title "Mellon Collie and the Infinite Sadness (Deluxe Edition)" :artist "Smashing Pumpkins"
                                                       :create-url "/create/album?itunes-album-id=721291853&"
                                                       :itunes-id  721291853 :thumbnail-url "http://is1.mzstatic.com/image/pf/us/r30/Music4/v4/cc/13/f1/cc13f183-1cfb-4880-23a1-859f9c938ac6/05099997854159.600x600-75.jpg"}
                                                      ]
                                            } "album")
             => [{
                  :author_id     nil,
                  :thumbnail_url "http://is1.mzstatic.com/image/pf/us/r30/Music4/v4/cc/13/f1/cc13f183-1cfb-4880-23a1-859f9c938ac6/05099997854159.600x600-75.jpg",
                  :username      nil,
                  :type          "album",
                  :title         "Mellon Collie and the Infinite Sadness (Deluxe Edition)",
                  :creation_date nil,
                  :id            nil,
                  :email_md5     nil,
                  :author        nil
                  :artist        "Smashing Pumpkins"
                  :platform      nil
                  :create-url    "/create/album?itunes-album-id=721291853&"
                  }]))

