(ns yswrl.test.swirls.swirls-repo-test
  (:require [yswrl.test.scaffolding :as s]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.swirls.swirl-links :as swirl-links])
  (:use clojure.test)
  (:use clj-http.fake)
  (:use yswrl.fake.faker))

(deftest swirls-repo-test

  (let [author (s/create-test-user)
        responder (s/create-test-user)
        non-responder (s/create-test-user)
        outsider (s/create-test-user)
        swirl (s/create-swirl "generic" (author :id) "Animals" "Yeah" [(responder :username) (non-responder :username) "nonuser@example.org"])
        draft-swirl (repo/save-draft-swirl "generic" (author :id) "Animals (draft)" "What to write...." nil)
        deleted-swirl-id (repo/delete-swirl (:id (s/create-swirl "generic" (author :id) "Gonna delete this" "I'm going to delete this" [])) (author :id))
        _ (repo/create-response (swirl :id) "HOT" responder)]

    (testing "Responses can be gotten and changed"
      (is (= [{:responder (responder :id),
               :email_md5 (responder :email_md5),
               :username  (responder :username),
               :summary   "HOT"}]
             (repo/get-swirl-responses (swirl :id))))
      (repo/create-response (swirl :id) "Not interested" responder)
      (is (= [{:responder (responder :id),
               :email_md5 (responder :email_md5),
               :username  (responder :username),
               :summary   "Not interested"}] (repo/get-swirl-responses (swirl :id))))
      (repo/create-response (swirl :id) "HOT" responder))


    (testing "get-swirl-if-allowed-to-view"
      (testing "allows the author to get the draft or live version"
        (is (not (nil? (repo/get-swirl-if-allowed-to-view (draft-swirl :id) (author :id)))))
        (is (not (nil? (repo/get-swirl-if-allowed-to-view (swirl :id) (author :id))))))
      (testing "does not allow non-authors to get the draft version whether they have responded or not or have been suggested to respond or not"
        (is (nil? (repo/get-swirl-if-allowed-to-view (draft-swirl :id) (responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-view (draft-swirl :id) (non-responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-view (draft-swirl :id) (outsider :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-view (draft-swirl :id) nil))))
      (testing "allows non-authors to get the live swirls"
        (is (not (nil? (repo/get-swirl-if-allowed-to-view (swirl :id) (responder :id)))))
        (is (not (nil? (repo/get-swirl-if-allowed-to-view (swirl :id) (non-responder :id)))))
        (is (not (nil? (repo/get-swirl-if-allowed-to-view (swirl :id) nil))))
        (is (not (nil? (repo/get-swirl-if-allowed-to-view (swirl :id) (outsider :id))))))
      (testing "does not allow anyone can see deleted swirls"
        (is (nil? (repo/get-swirl-if-allowed-to-view deleted-swirl-id (author :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-view deleted-swirl-id (responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-view deleted-swirl-id (non-responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-view deleted-swirl-id nil)))
        (is (nil? (repo/get-swirl-if-allowed-to-view deleted-swirl-id (outsider :id))))))

    (testing "get-swirl-if-allowed-to-edit"
      (testing "allows the author to edit the draft or live version"
        (is (not (nil? (repo/get-swirl-if-allowed-to-edit (draft-swirl :id) (author :id)))))
        (is (not (nil? (repo/get-swirl-if-allowed-to-edit (swirl :id) (author :id))))))
      (testing "does not allow non-authors to edit"
        (is (nil? (repo/get-swirl-if-allowed-to-edit (draft-swirl :id) (responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-edit (draft-swirl :id) (non-responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-edit (draft-swirl :id) (outsider :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-edit (draft-swirl :id) nil))))
      (testing "does not allow non-authors to edit the live swirls"
        (is (nil? (repo/get-swirl-if-allowed-to-edit (swirl :id) (responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-edit (swirl :id) (non-responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-edit (swirl :id) nil)))
        (is (nil? (repo/get-swirl-if-allowed-to-edit (swirl :id) (outsider :id)))))
      (testing "does not allow anyone can see deleted swirls"
        (is (nil? (repo/get-swirl-if-allowed-to-edit deleted-swirl-id (author :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-edit deleted-swirl-id (responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-edit deleted-swirl-id (non-responder :id))))
        (is (nil? (repo/get-swirl-if-allowed-to-edit deleted-swirl-id nil)))
        (is (nil? (repo/get-swirl-if-allowed-to-edit deleted-swirl-id (outsider :id))))))

    (testing "the same user can not be suggested twice"
      (println "Adding" (swirl :id))
      (repo/add-suggestions (swirl :id) (author :id) [(non-responder :username)])
      (is (= [{:username (responder :username) :user-id (responder :id)}
              {:username (non-responder :username) :user-id (non-responder :id)}]
             (repo/get-suggestion-usernames (swirl :id))))
      (is (= 1 (count (repo/get-non-responders (swirl :id))))))

    (testing "get-swirls-awaiting-response"
      (testing "returns non-responded swirls when the user has pending swirls to respond to"
        (is (= 1 (repo/get-swirls-awaiting-response-count (non-responder :id))))
        (let [results (repo/get-swirls-awaiting-response (non-responder :id) 100 0)]
          (is (= 1 (count results)))
          (is (= ((first results) :id) (swirl :id)))))

      (testing "returns an empty list if all swirls are responded to"
        (is (= 0 (repo/get-swirls-awaiting-response-count (responder :id))))
        (let [results (repo/get-swirls-awaiting-response (responder :id) 100 0)]
          (is (= 0 (count results)))
          )))

    (testing "get-swirls-by-response"
      (testing "returns an empty list if there are no responses for that user"
        (is (= 0 (count (repo/get-swirls-by-response (non-responder :id) 100 0 "HOT"))))
        (is (= 0 (count (repo/get-swirls-by-response (responder :id) 100 0 "LOVINGITYEAH")))))
      (testing "returns selected responses by case-insensitive lookup"
        (let [results (repo/get-swirls-by-response (responder :id) 100 0 "HOT")]
          (is (= 1 (count results)))
          (is (= ((first results) :id) (swirl :id)))))
      (testing "lookups are case insensitive"
        (is (= (repo/get-swirls-by-response (responder :id) 100 0 "HOT")
               (repo/get-swirls-by-response (responder :id) 100 0 "hOt")))))

    (testing "links can be added and when retrieved the type information is appended"
      (let [_ (repo/add-link (swirl :id) "I" "123456781234567812345678")
            _ (repo/add-link (swirl :id) "A" "SJJKSDHFJKSDHFJK")
            swirl-links (repo/get-links (swirl :id))
            itunes (first swirl-links)]
        (is (= 2 (count swirl-links)))
        (is (= swirl-links/itunes-id (itunes :type)))))

    (testing "all responses for a user can be gotten"
      (is (= [{:summary "HOT" :count 1}] (repo/get-response-count-for-user (responder :id))))
      (is (= [] (repo/get-response-count-for-user (non-responder :id)))))

    (testing "most recent respones by type can be gotten"
      (is (= ["HOT"] (repo/get-recent-responses-by-user-and-type (responder :id) (swirl :type) ["blah"])))
      (is (= [] (repo/get-recent-responses-by-user-and-type (responder :id) (swirl :type) ["HOT"])))
      (is (= [] (repo/get-recent-responses-by-user-and-type (responder :id) "whatever" []))))

    (testing "People who have not responded can be got"
      (is (= [{:email_md5 (non-responder :email_md5),
               :username  (non-responder :username)}] (repo/get-non-responders (swirl :id)))))))