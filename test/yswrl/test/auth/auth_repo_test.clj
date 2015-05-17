(ns yswrl.test.auth.auth-repo-test
  (:require [yswrl.auth.auth-repo :as repo]
            [yswrl.test.scaffolding :as s]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :refer :all])
  (:use clojure.test))

(deftest auth-repo-test
  (testing "Can tell you if a username doesn't exist"
    (is (not (repo/user-exists "non-existant-user-234"))))

  (testing "Can tell you if a username does exist"
    (let [user (s/create-test-user)]
      (is (repo/user-exists (user :username)))))

  (testing "Username suggestions take the desired name if it's not already taken"
      (is (= "non-existant-user-234" (repo/suggest-username "non-existant-user-234"))))

  (testing "When users are created their email is hashed and stored"
    (let [username (s/unique-username)
          email (s/unique-email username)
          user (repo/create-user username email s/test-user-password)
          expected-hash (-> (hash/md5 email)
                            (bytes->hex))]
      (is (= expected-hash (user :email_md5)))))

  (testing "Username suggestions append a number if the desired name is taken"
    (let [user (s/create-test-user)
          next-username (str (user :username) "1")]
      (is (= next-username (repo/suggest-username (user :username))))
      (s/create-test-user :username next-username)
      (is (= (str (user :username) "2") (repo/suggest-username (user :username)))))))
