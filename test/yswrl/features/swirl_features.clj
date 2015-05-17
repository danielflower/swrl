(ns yswrl.features.swirl-features
  (:require [yswrl.handler :refer [app]]
            [yswrl.test.scaffolding :as s]
            [yswrl.links :as linky]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))
(selmer.parser/cache-off!)

(defn now [] (System/currentTimeMillis))



(defn login-as [visit user]
  (-> visit
      (fill-in "Username" (user :username))
      (fill-in "Password" s/test-user-password)
      (press "Login")
      (follow-redirect)))

(defn save-url [session map key]
  (let [url (get-in session [:request :uri])]
    (swap! map (fn [old-val] (assoc old-val key url))))
  session)

(defn log-out [session]
  (-> session
      (follow "Log out")
      (follow-redirect)))

(defn submit [session name]
  (-> session
      (press name)
      (follow-redirect)))


(defn assert-swirl-title-in-header [session title]
  (-> session
      (within [:h1]
              (has (text? (str "You should consume " title))))
      ))


(deftest swirl-security
  (with-faked-responses
  (let [user1 (s/create-test-user)
        user2 (s/create-test-user)
        test-state (atom {})]

    (-> (session app)
        (visit "/")
        ; Login as user 1
        (follow "Login")
        (login-as user1)

        ; Create a swirl
        (follow "Create")
        (fill-in "Enter a YouTube video URL" "https://www.youtube.com/watch?v=TllPrdbZ-VI")
        (submit "Go")

        (save-url test-state :edit-swirl-uri)

        (submit "Submit")

        (save-url test-state :view-swirl-uri)

        (assert-swirl-title-in-header "How to chop an ONION using CRYSTALS with Jamie Oliver")

        (follow "[edit this page]")
        (fill-in "You should watch or read or listen to" "The onion video")
        (submit "Submit")

        (assert-swirl-title-in-header "The onion video")

        (log-out)

        (follow "Login")
        (login-as user2)

        ; Other users can view the swirl....
        (visit (@test-state :view-swirl-uri))
        (within [:h1]
                (has (text? "You should consume The onion video")))
        (has (missing? [:.swirl-admin-panel]))

        ; ...but they can't edit the page
        (visit (@test-state :edit-swirl-uri))
        (has (status? 404))
        (has (text? "Not Found"))
        ))))


(deftest itunes-album-swirl-creation
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/swirls/start")

          (fill-in "Album or Song Title" "Mellon Collie")
          (press :#album-search-go-button)

          (follow "Mellon Collie and the Infinite Sadness (Remastered)")

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          (fill-in "You should watch or read or listen to" "Mellon Collie and the Infinite Sadness")
          (submit "Submit")

          (assert-swirl-title-in-header "Mellon Collie and the Infinite Sadness")
          ))))

(deftest quick-login
  (let [user (s/create-test-user)
        swirl (s/create-swirl (user :id) "Great swirls" "This is a great swirl" [], {})]
    (-> (session app)

        ; when not logged in, the page can be viewed
        (visit (linky/swirl (swirl :id)))
        (assert-swirl-title-in-header (swirl :title))

        ; ...and the user can log in from the page and be redirected
        (login-as user)
        (assert-swirl-title-in-header (swirl :title))

        )))

(deftest quick-register
  (let [user (s/create-test-user)
        swirl (s/create-swirl (user :id) "Great swirls" "This is a great swirl" [], {})]
    (-> (session app)

        ; when not logged in, the page can be viewed
        (visit (linky/swirl (swirl :id)))
        (assert-swirl-title-in-header (swirl :title))

        ; ...and the user can register from the page and be redirected
        (fill-in "Username" (str "Ampter-Jamp" (s/now)))
        (fill-in "Email" (str "ampter" (s/now) "@example.org"))
        (fill-in "Password" "A#~$&#(@*~$&__f 1234")
        (submit "Register")

        (assert-swirl-title-in-header (swirl :title))

        )))