(ns yswrl.features.swirl-features
  (:require [yswrl.handler :refer [app]]
            [yswrl.test.scaffolding :as s]
            [yswrl.links :as linky]
            [yswrl.features.actions :as actions]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.db :as db]
            [yswrl.links :as links])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))
(use 'korma.core)
(use 'korma.db)

(selmer.parser/cache-off!)

(defn now [] (System/currentTimeMillis))



(defn login-as [visit user]
  (-> visit
      (fill-in "Username" (user :username))
      (fill-in "Password" s/test-user-password)
      (press "Login")
      (follow-redirect)))


(defn save-state [session map key value]
    (swap! map (fn [old-val] (assoc old-val key value)))
  session)

(defn save-url [session map key]
  (let [url (get-in session [:request :uri])]
    (save-state session map key url))
  session)



(defn assert-swirl-title-in-header [session verb title]
  (-> session
      (within [:h1]
              (has (text? title)))
      ))

(deftest swirl-security
  (with-faked-responses
    (let [user1 (s/create-test-user)
          user2 (s/create-test-user)
          test-state (atom {})]

      (-> (session app)
          (visit "/")
          ; Login as user 1
          (actions/follow-login-link)
          (login-as user1)

          ; Create a swirl
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")

          (save-url test-state :edit-swirl-uri)

          (actions/save-swirl)

          (save-url test-state :view-swirl-uri)

          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

          (follow "Edit Swirl")
          (fill-in "You should watch" "The onion video")
          (actions/save-swirl)
          (assert-swirl-title-in-header "watch" "The onion video")

          ; the delete page can be browsed to, and cancelling works

          (follow "Delete")
          (within [:h1]
                  (has (text? "Delete a swirl")))
          (save-url test-state :delete-swirl-uri)
          (follow "Cancel")
          (assert-swirl-title-in-header "watch" "The onion video")

          (actions/log-out)
          (actions/follow-login-link)
          (login-as user2)

          ; Other users can view the swirl....
          (visit (@test-state :view-swirl-uri))
          (assert-swirl-title-in-header "watch" "The onion video")
          (has (missing? [:.swirl-admin-panel]))

          ; ...but they can't edit the page
          (visit (@test-state :edit-swirl-uri))
          (has (status? 404))
          (has (text? "Not Found"))

          ; ...nor attempt to delete it
          (visit (@test-state :delete-swirl-uri))
          (has (status? 404))
          (has (text? "Not Found"))

          ; But the author can delete it
          (visit "/")
          (actions/log-out)
          (actions/follow-login-link)
          (login-as user1)
          (visit (@test-state :delete-swirl-uri))
          (actions/submit "Confirm deletion")

          ; You are taken to your profile page after deleting
          (within [:h1]
                  (has (text? (str "Reviews by " (user1 :username)))))

          ; ...and it's now deleted
          (visit (@test-state :view-swirl-uri))
          (has (status? 404))

          ))))



(deftest a-user-that-registers-as-a-result-of-a-suggestion-email-has-a-network-and-notifications-set-up
  (with-faked-responses
    (let [existing-user (s/create-test-user)
          new-user-username (s/unique-username)
          new-user-email (str new-user-username "@example.org")
          test-state (atom {})]

      (-> (session app)
          (visit "/")
          ; Login as user 1
          (actions/follow-login-link)
          (login-as existing-user)

          ; Create a couple of swirls and add the non-registered user's email to both of them
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")
          (fill-in "You should watch" "The onion video")
          (fill-in :.recipients new-user-email)
          (actions/save-swirl)
          (save-url test-state :view-swirl-uri)
          (save-state test-state :suggestion-code (:code (first (select db/suggestions (fields :code) (where {:recipient_email new-user-email})))))

          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/vimeo.3718294.html")
          (actions/submit "Go")
          (fill-in "You should watch" "Vimeo video")
          (fill-in :.recipients new-user-email)
          (actions/save-swirl)

          (actions/log-out)

          ; now simulate the friend clicking on a link in their email to the first swirl
          (visit (str (@test-state :view-swirl-uri) "?code=" (@test-state :suggestion-code)))

          ; the username and email box should be pre-filled, so just enter the password
          (fill-in :.registration-password-field "p@ssw0rd mania")
          (actions/submit "Register")

          ; the original user should now show up on the new user's create-swirl page
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/vimeo.3718294.html")
          (actions/submit "Go")
          (check (existing-user :username))

          ; Can immediately make a response and it will be in the response inbox
          (visit (@test-state :view-swirl-uri))
          (actions/submit "Loved it")
          (visit (links/inbox "Loved it"))
          (follow "The onion video")

          ; Meanwhile, in the inbox there should be the second swirl
          (visit (links/inbox))
          (follow "Vimeo video")


          ;(assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          ;
          ;(follow "Edit Swirl")
          ;(fill-in "You should watch" "The onion video")
          ;(actions/save-swirl)
          ;(assert-swirl-title-in-header "watch" "The onion video")
          ;
          ;; the delete page can be browsed to, and cancelling works
          ;
          ;(follow "Delete")
          ;(within [:h1]
          ;        (has (text? "Delete a swirl")))
          ;(save-url test-state :delete-swirl-uri)
          ;(follow "Cancel")
          ;(assert-swirl-title-in-header "watch" "The onion video")
          ;
          ;(actions/log-out)
          ;(actions/follow-login-link)
          ;(login-as user2)
          ;
          ;; Other users can view the swirl....
          ;(visit (@test-state :view-swirl-uri))
          ;(assert-swirl-title-in-header "watch" "The onion video")
          ;(has (missing? [:.swirl-admin-panel]))
          ;
          ;; ...but they can't edit the page
          ;(visit (@test-state :edit-swirl-uri))
          ;(has (status? 404))
          ;(has (text? "Not Found"))
          ;
          ;; ...nor attempt to delete it
          ;(visit (@test-state :delete-swirl-uri))
          ;(has (status? 404))
          ;(has (text? "Not Found"))
          ;
          ;; But the author can delete it
          ;(visit "/")
          ;(actions/log-out)
          ;(actions/follow-login-link)
          ;(login-as existing-user)
          ;(visit (@test-state :delete-swirl-uri))
          ;(actions/submit "Confirm deletion")
          ;
          ;; You are taken to your profile page after deleting
          ;(within [:h1]
          ;        (has (text? (str "Reviews by " (existing-user :username)))))
          ;
          ;; ...and it's now deleted
          ;(visit (@test-state :view-swirl-uri))
          ;(has (status? 404))

          ))))



(deftest itunes-album-swirl-creation
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/")
          (actions/follow-create-link)

          (fill-in "Album or Song Title" "Mellon Collie")
          (press :#album-search-go-button)

          (follow "Mellon Collie and the Infinite Sadness (Remastered)")

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          (fill-in "You should listen to" "Mellon Collie and the Infinite Sadness")
          (actions/save-swirl)

          (assert-swirl-title-in-header "listen to" "Mellon Collie and the Infinite Sadness")

          (within [:title] (has (text? "You should listen to Mellon Collie and the Infinite Sadness")))

          ))))


(deftest website-swirl-creation
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/swirls/start")

          (fill-in "Enter a website link" "http://jakearchibald.com/2013/progressive-enhancement-still-important/")
          (press :#website-create-go-button)

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          (fill-in "You should see" "A website")
          (actions/save-swirl)

          (assert-swirl-title-in-header "see" "A website")

          (within [:title] (has (text? "You should see A website")))

          ))))

(deftest movie-swirl-creation
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/swirls/start")

          (fill-in "Movie Title" "garden state")
          (press :#movie-search-go-button)

          (follow "Garden State")

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          ;Don't need to fill in as should be this by default:
          ;(fill-in "You should watch" "Garden State")
          (actions/save-swirl)

          (assert-swirl-title-in-header "watch" "Garden State")

          (within [:title] (has (text? "You should watch Garden State")))

          ))))

(deftest tv-swirl-creation
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/swirls/start")

          (fill-in "TV Show Title" "black mirror")
          (press :#tv-search-go-button)

          (follow "Black Mirror")

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          ;Don't need to fill in as should be this by default:
          ;(fill-in "You should watch" "Garden State")
          (actions/save-swirl)

          (assert-swirl-title-in-header "watch" "Black Mirror")

          (within [:title] (has (text? "You should watch Black Mirror")))

          ))))

(deftest website-swirl-creation-from-tmdb-link
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/swirls/start")

          (fill-in "Enter a website link" "https://www.themoviedb.org/movie/401-garden-state")
          (press :#website-create-go-button)

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          ;Don't need to fill in as should be this by default:
          ;(fill-in "You should watch" "Garden State")
          (actions/save-swirl)

          (assert-swirl-title-in-header "watch" "Garden State")

          (within [:title] (has (text? "You should watch Garden State")))

          ))))

(deftest website-swirl-creation-from-imdb-link-movie
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/swirls/start")

          (fill-in "Enter a website link" "http://www.imdb.com/title/tt0333766")
          (press :#website-create-go-button)

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          ;Don't need to fill in as should be this by default:
          ;(fill-in "You should watch" "Garden State")
          (actions/save-swirl)

          (assert-swirl-title-in-header "watch" "Garden State")

          (within [:title] (has (text? "You should watch Garden State")))

          ))))


(deftest website-swirl-creation-from-imdb-link-tv
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/swirls/start")

          (fill-in "Enter a website link" "http://www.imdb.com/title/tt2085059")
          (press :#website-create-go-button)

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          ;Don't need to fill in as should be this by default:
          ;(fill-in "You should watch" "Black Mirror")
          (actions/save-swirl)

          (assert-swirl-title-in-header "watch" "Black Mirror")

          (within [:title] (has (text? "You should watch Black Mirror")))

          ))))
(deftest website-swirl-creation-with-title-extraction
  (with-faked-responses
    (let [user (s/create-test-user)]

      (-> (session app)
          (visit "/swirls/start")

          (fill-in "Enter a website link" "http://jakearchibald.com/2013/progressive-enhancement-still-important/")
          (press :#website-create-go-button)

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          ;Don't need to fill in as should be this by default:
          ;(fill-in "You should watch" "Garden State")
          (actions/save-swirl)

          (assert-swirl-title-in-header "see" "Progressive enhancement is still important - JakeArchibald.com")

          (within [:title] (has (text? "You should see Progressive enhancement is still important - JakeArchibald.com")))

          ))))


(deftest quick-login
  (let [user (s/create-test-user)
        swirl (s/create-swirl "generic" (user :id) "Great swirls" "This is a great swirl" [])]
    (-> (session app)

        ; when not logged in, the page can be viewed
        (visit (linky/swirl (swirl :id)))
        (assert-swirl-title-in-header "see" (swirl :title))

        ; ...and the user can log in from the page and be redirected
        (login-as user)
        (assert-swirl-title-in-header "see" (swirl :title))

        )))

(deftest quick-register
  (let [user (s/create-test-user)
        swirl (s/create-swirl "generic" (user :id) "Great swirls" "This is a great swirl" [])
        consumption-verb "see"]
    (-> (session app)

        ; when not logged in, the page can be viewed
        (visit (linky/swirl (swirl :id)))
        (assert-swirl-title-in-header consumption-verb (swirl :title))

        ; ...and the user can register from the page and be redirected
        (fill-in "Username" (str "Ampter-Jamp" (s/now)))
        (fill-in "Email" (str "ampter" (s/now) "@example.org"))
        (fill-in "Password" "A#~$&#(@*~$&__f 1234")
        (actions/submit "Register")

        (assert-swirl-title-in-header consumption-verb (swirl :title))

        )))

(deftest swirl-responses
  (let [author (s/create-test-user)
        responder (s/create-test-user)
        non-responder (s/create-test-user)
        swirl (s/create-swirl "generic" (author :id) "Animals" "Yeah" [(responder :username) (non-responder :username) "nonuser@example.org"])
        _ (repo/respond-to-swirl (swirl :id) "HOT" responder)]
    (-> (session app)
        (visit "/")
        (actions/follow-login-link)
        (login-as author)

        (visit (linky/swirl (swirl :id)))
        (within [:.response] (has (some-text? (str (responder :username) " said HOT"))))
        (within [:.non-responders] (has (some-text? (str "Yet to respond: " (non-responder :username)))))

        )))


(deftest swirl-notifications
  (with-faked-responses
    (let [author (s/create-test-user)
          recipient (s/create-test-user)]

      (-> (session app)
          (visit "/")
          ; Login as the author
          (actions/follow-login-link)
          (login-as author)

          ; Create a swirl
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")

          (fill-in :.recipients (recipient :username))
          (actions/save-swirl)

          (within [:.non-responders :.username]
                  (has (text? (recipient :username))))

          (actions/log-out)
          (visit (links/notifications))
          (follow-redirect)
          (login-as recipient)

          (within [:h1]
                  (has (text? "What's new")))

          (follow "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

          ; returning back to the notification page, the notification, having been seen, should have disappeared
          (visit (links/notifications))

          (within [:h1]
                  (has (text? "What's new")))
          (within [:.notifications :p]
                  (has (text? "Nothing to see here")))
          ))))

(deftest extension-from-chrome-page-redirects-to-homepage
  (-> (session app)
      (visit "/create/from-url?url=chrome%3A%2F%2Fnewtab%2F&title=New%20Tab")
      (follow-redirect)
      (within [:h1]
              (has (text? "Welcome to Swirl")))))