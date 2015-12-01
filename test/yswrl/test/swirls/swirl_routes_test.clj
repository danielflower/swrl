(ns yswrl.test.swirls.swirl-routes-test
  (:require [yswrl.test.scaffolding :as s]
            [yswrl.swirls.swirl-routes :as routes]
            [yswrl.groups.groups-repo :as groups-repo]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.user.notifications-repo :as notifications-repo]
            [yswrl.user.notifications :as notifications])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker))

(deftest swirls-repo-test

  (let [author (s/create-test-user)
        recipient (s/create-test-user)
        group (groups-repo/create-group (author :id) "My Group" "This is a group for the ages")
        _ (groups-repo/add-group-member (group :id) (recipient :id))
        _ (groups-repo/add-group-member (group :id) (author :id))
        draft-swirl (repo/save-draft-swirl "generic" (author :id) "Animals (draft)" "What to write...." nil)]

    (testing "Users can be added via the select box and via groups and they are notified only once"
      (routes/publish-swirl author (draft-swirl :id) ["somebody"] "The published swirl" "It is a great swirl" nil [(group :id)] false "movie")
      (is (= (map :notification_type (notifications-repo/get-for-user-page (recipient :id))) [notifications/recommendation]))
      (routes/publish-swirl author (draft-swirl :id) ["somebody"] "The published swirl" "It is a great swirl" nil [(group :id)] false "movie")
      (is (= (map :notification_type (notifications-repo/get-for-user-page (recipient :id))) [notifications/recommendation]))
      (is (empty? (notifications-repo/get-for-user-page (author :id))))
      (is (= [(recipient :id)] (map :user-id (repo/get-suggestion-usernames (draft-swirl :id)))))
      )))
