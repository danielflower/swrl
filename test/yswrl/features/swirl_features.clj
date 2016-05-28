(ns yswrl.features.swirl-features
  (:require [yswrl.handler :refer [app]]
            [yswrl.test.scaffolding :as s]
            [net.cgrand.enlive-html :as enlive]
            [yswrl.links :as linky]
            [yswrl.features.actions :as actions]
            [kerodon.core :refer :all]
            [kerodon.impl :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]
            [yswrl.swirls.swirls-repo :as repo]
            [yswrl.db :as db]
            [yswrl.links :as links]
            [yswrl.swirls.lookups :as lookups]
            [korma.core
             :refer [insert values where join fields set-fields select raw modifier]])
  (:use clj-http.fake)
  (:use yswrl.fake.faker))
(use 'korma.db)

(selmer.parser/cache-off!)

(defn now [] (System/currentTimeMillis))

(defn test-log [session message]
  (println message)
  session)

(defn print-session [session]
  (println session)
  session)

(defn login-as [visit user]
  (actions/login-as visit user))


(defn save-state [session map key value]
  (swap! map (fn [old-val] (assoc old-val key value)))
  session)

(defn save-url [session map key]
  (let [url (get-in session [:request :uri])]
    (save-state session map key url))
  session)

(defn save-swirl-id [session map key]
  (let [url (get-in session [:request :uri])
        [_ swirl-id] (re-find #".*/swirls/([\d]+)" url)
        swirl-id (Long/parseLong swirl-id)]
    (save-state session map key swirl-id))
  session)

(defn assert-user-checkbox-is-checked [session user]
  (is (= "checked"
         (get-attr session [(enlive/attr= :value
                                          (user :username))] :checked))
      "User checkbox should be checked")
  session)

(defn assert-default-selection-is-value [session selection]
  (is (= "selected"
         (get-attr session [(enlive/attr= :value selection)] :selected))
      (str "Selected item should be " selection))
  session)

(defn assert-is-private-checkbox-is-checked [session]
  (is (= "checked"
         (get-attr session [:.private-toggle] :checked))
      "Private Toggle should be checked")
  session)

(defn assert-is-private-checkbox-is-not-checked [session]
  (is (not (= "checked"
              (get-attr session [:.private-toggle] :checked)))
      "Private Toggle should not be checked")
  session)

(defn assert-number-of-comments [session swirl-id number-to-check]
  (is (= number-to-check
         (count (repo/get-swirl-comments swirl-id))))
  session)

(defn assert-number-of-links [session swirl-id number-to-check]
  (is (= number-to-check
         (count (repo/get-links swirl-id))))
  session)

(defn assert-swirl-type [session swirl-id type-to-check]
  (is (= type-to-check
         (:type (lookups/get-swirl swirl-id))))
  session)

(defn assert-swirl-title-in-header [session verb title]
  (-> session
      (within [:h1]
              (has (some-text? title)))
      ))

(defn cannot-follow [session selector]
  (if (= :cannot-follow (try (follow session selector)
                             (catch IllegalArgumentException _
                               :cannot-follow)))
    session
    (do (println "Shouldn't be able to follow link: " selector)
        (throw Exception))))

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
          (fill-in :#swirl-title "The onion video")
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
                  (has (some-text? (str (:username user1)))))

          ; ...and it's now deleted
          (visit (@test-state :view-swirl-uri))
          (has (status? 404))

          ))))



(deftest empty-inbox-test
  (let [new-user (s/create-test-user)]
    (-> (session app)
        (visit (links/inbox))
        (follow-redirect)
        ; Login as user 1
        (login-as new-user)


        ; Their inbox is empty
        (visit (links/inbox))
        (within [:p] (has (some-text? "We've run out of swirls. Gomen Nasai")))

        ; Make sure the links work
        (follow "create a recommendation for one of your friends")
        (within [:h1] (has (text? "Recommend something")))

        (visit (links/inbox))

        )))

(deftest respond-to-swirl-with-swirl
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

          (actions/save-swirl)

          (save-url test-state :view-swirl-uri)
          (save-swirl-id test-state :swirl-id)

          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

          (assert-number-of-comments (@test-state :swirl-id) 0)
          (assert-number-of-links (@test-state :swirl-id) 1)

          ; Now login as another user

          (actions/log-out)
          (actions/follow-login-link)
          (login-as user2)

          ; Other users can view the swirl and press the respond to swirl with swirl button!
          (visit (@test-state :view-swirl-uri))
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

          (press :#respond-with-swirl)

          ; should take the user to the create swirl page - modified for responses

          (within [:h1] (has (text? "Respond with Swirl")))
          (within [:.standard-page :p] (has (text? "What would you like to respond with?")))

          ; respond with website link
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")

          ; user1 should be pre-selected as a recipient
          (assert-user-checkbox-is-checked user1)


          (actions/save-swirl)

          ; Upon save, the user should be taken back to the original swirl where there should be a new comment
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-number-of-comments (@test-state :swirl-id) 1)

          ;there should also be an extra link on this page

          (assert-number-of-links (@test-state :swirl-id) 2)


          ;now respond with music search
          (press :#respond-with-swirl)


          (fill-in "Album or Song Title" "Mellon Collie")
          (press :#album-search-go-button)
          (within [:h1] (has (text? "Select an item to recommend")))

          (follow "Mellon Collie and the Infinite Sadness (Remastered)")
          (follow-redirect)
          (within [:h1] (has (text? "Create a swirl")))


          ; user1 should be pre-selected as a recipient

          (assert-user-checkbox-is-checked user1)

          (actions/save-swirl)

          ; Upon save, the user should be taken back to the original swirl where the should be a new comment
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-number-of-comments (@test-state :swirl-id) 2)

          ;there should also be an extra link on this page

          (assert-number-of-links (@test-state :swirl-id) 3)

          ; now respond with movie search
          (press :#respond-with-swirl)

          (fill-in "Movie Title" "garden state")
          (press :#movie-search-go-button)

          (follow "Garden State (2004)")
          (follow-redirect)
          ; user1 should be pre-selected as a recipient

          (assert-user-checkbox-is-checked user1)

          (actions/save-swirl)

          ; Upon save, the user should be taken back to the original swirl where the should be a new comment
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-number-of-comments (@test-state :swirl-id) 3)

          ;there should also be an extra link on this page

          (assert-number-of-links (@test-state :swirl-id) 4)
          ;; now respond with tv search
          (press :#respond-with-swirl)

          (fill-in "TV Show Title" "black mirror")
          (press :#tv-search-go-button)

          (follow "Black Mirror")
          (follow-redirect)

          ; user1 should be pre-selected as a recipient

          (assert-user-checkbox-is-checked user1)

          (actions/save-swirl)

          ; Upon save, the user should be taken back to the original swirl where the should be a new comment
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-number-of-comments (@test-state :swirl-id) 4)

          ;there should also be an extra link on this page

          (assert-number-of-links (@test-state :swirl-id) 5)

          ;; now respond with website which is amazon.com
          ;;(press :#respond-with-swirl)
          ;
          ;
          ;; user1 should be pre-selected as a recipient
          ;
          ;;(assert-user-checkbox-is-checked user1)
          ;
          ;; now respond with website which is itunes
          (press :#respond-with-swirl)

          (fill-in "Enter a website link" "https://itunes.apple.com/us/album/am/id721224313")
          (press :#website-create-go-button)

          (follow-redirect)

          ; user1 should be pre-selected as a recipient

          (assert-user-checkbox-is-checked user1)
          (actions/save-swirl)

          ; Upon save, the user should be taken back to the original swirl where the should be a new comment
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-number-of-comments (@test-state :swirl-id) 5)

          ;there should also be an extra link on this page

          (assert-number-of-links (@test-state :swirl-id) 6)

          ;; now respond with website which is tmdb
          (press :#respond-with-swirl)

          (fill-in "Enter a website link" "https://www.themoviedb.org/movie/401-garden-state")
          (press :#website-create-go-button)
          (follow-redirect)

          ; user1 should be pre-selected as a recipient

          (assert-user-checkbox-is-checked user1)

          (actions/save-swirl)

          ; Upon save, the user should be taken back to the original swirl where the should be a new comment
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-number-of-comments (@test-state :swirl-id) 6)

          ;there should also be an extra link on this page

          (assert-number-of-links (@test-state :swirl-id) 7)
          ;; now respond with website which is imdb movie
          (press :#respond-with-swirl)

          (fill-in "Enter a website link" "http://www.imdb.com/title/tt0333766")
          (press :#website-create-go-button)
          (follow-redirect)


          ; user1 should be pre-selected as a recipient

          (assert-user-checkbox-is-checked user1)
          (actions/save-swirl)

          ; Upon save, the user should be taken back to the original swirl where the should be a new comment
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-number-of-comments (@test-state :swirl-id) 7)

          ;there should also be an extra link on this page

          (assert-number-of-links (@test-state :swirl-id) 8)

          ;
          ;; now respond with website which is imdb tv
          (press :#respond-with-swirl)

          (fill-in "Enter a website link" "http://www.imdb.com/title/tt2085059")
          (press :#website-create-go-button)
          (follow-redirect)


          ; user1 should be pre-selected as a recipient

          (assert-user-checkbox-is-checked user1)

          (actions/save-swirl)

          ; Upon save, the user should be taken back to the original swirl where the should be a new comment
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-number-of-comments (@test-state :swirl-id) 8)

          ;there should also be an extra link on this page

          (assert-number-of-links (@test-state :swirl-id) 9)

          ;; finally, go to create page directly to ensure normal titles haven't changed
          (actions/follow-create-link)
          (within [:h1] (has (text? "Recommend something")))
          (within [:.standard-page :p] (has (text? "What would you like to recommend?")))

          ))))

(deftest can-reswirl-an-existing-swirl
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

          (actions/save-swirl)

          (save-url test-state :view-swirl-uri)
          (save-swirl-id test-state :swirl-id)

          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

          ; Now login as another user

          (actions/log-out)
          (actions/follow-login-link)
          (login-as user2)

          ; Other users can view the swirl and press the respond to swirl with swirl button!
          (visit (@test-state :view-swirl-uri))
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

          (press :#re-swirl)

          (follow-redirect)
          (actions/save-swirl)

          ;the new swirl has the same title
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

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
          (fill-in :#swirl-title "The onion video")
          (fill-in :.recipients new-user-email)
          (actions/save-swirl)
          (save-url test-state :view-swirl-uri)
          (save-state test-state :suggestion-code (:code (first (select db/suggestions (fields :code) (where {:recipient_email new-user-email})))))

          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/vimeo.3718294.html")
          (actions/submit "Go")
          (fill-in :#swirl-title "Vimeo video")
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
          (assert-user-checkbox-is-checked existing-user)

          ; Can immediately make a response and it will be in the response inbox
          (visit (@test-state :view-swirl-uri))
          (actions/submit [(enlive/attr= :value "Loved it")])
          (visit (links/user new-user-username))
          (follow "Loved it")
          (follow "The onion video")

          ; Meanwhile, in the inbox there should be the second swirl
          (visit (links/inbox))
          (follow "Vimeo video")

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

          (fill-in :#swirl-title "Mellon Collie and the Infinite Sadness")
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

          (fill-in :#swirl-title "A website")
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

          (follow "Garden State (2004)")

          ; Not logged in, so expect login page redirect
          (follow-redirect)

          (login-as user)
          (follow-redirect)

          ;Don't need to fill in as should be this by default:
          ;(fill-in "You should watch" "Garden State (2004)")
          (actions/save-swirl)

          (assert-swirl-title-in-header "watch" "Garden State (2004)")

          (within [:title] (has (text? "You should watch Garden State (2004)")))

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
          ;(fill-in "You should watch" "Black Mirror")
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
          ;(fill-in "You should watch" "Garden State (2004)")
          (actions/save-swirl)

          (assert-swirl-title-in-header "watch" "Garden State (2004)")

          (within [:title] (has (text? "You should watch Garden State (2004)")))

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
          ;(fill-in "You should watch" "Garden State (2004)")
          (actions/save-swirl)

          (assert-swirl-title-in-header "watch" "Garden State (2004)")

          (within [:title] (has (text? "You should watch Garden State (2004)")))

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
          ;(fill-in "You should watch" "Garden State (2004)")
          (actions/save-swirl)

          (assert-swirl-title-in-header "see" "Progressive enhancement is still important - JakeArchibald.com")

          (within [:title] (has (text? "You should see Progressive enhancement is still important - JakeArchibald.com")))

          ))))


(deftest quick-login
  (let [user (s/create-test-user)
        swirl (s/create-swirl "website" (user :id) "Great swirls" "This is a great swirl" [])]
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
        swirl (s/create-swirl "website" (user :id) "Great swirls" "This is a great swirl" [])
        consumption-verb "see"]
    (-> (session app)

        ; when not logged in, the page can be viewed
        (visit (linky/swirl (swirl :id)))
        (assert-swirl-title-in-header consumption-verb (swirl :title))

        ; ...and the user can register from the page and be redirected
        (fill-in "Username or email" (str "Ampter-Jamp" (s/now)))
        (fill-in "Email" (str "ampter" (s/now) "@example.org"))
        (fill-in "Password" "A#~$&#(@*~$&__f 1234")
        (actions/submit "Register")

        (assert-swirl-title-in-header consumption-verb (swirl :title))

        )))

(deftest swirl-responses
  (let [author (s/create-test-user)
        responder (s/create-test-user)
        non-responder (s/create-test-user)
        swirl (s/create-swirl "website" (author :id) "Animals" "Yeah" [(responder :username) (non-responder :username) "nonuser@example.org"])
        _ (repo/respond-to-swirl (swirl :id) "HOT" responder)]
    (-> (session app)
        (visit "/")
        (actions/follow-login-link)
        (login-as author)

        (visit (linky/swirl (swirl :id)))
        (within [:.comment-body] (has (some-text? "HOT")))
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
          (within [:span.new]
                  (has (some-text? "New")))

          (follow "How to chop an ONION using CRYSTALS with Jamie Oliver")
          (assert-swirl-title-in-header "watch" "How to chop an ONION using CRYSTALS with Jamie Oliver")

          ; returning back to the notification page, the notification, having been seen, should not have the 'new' tag
          (visit (links/notifications))

          (within [:h1]
                  (has (text? "What's new")))
          (within [:span.new]
                  (has (text? "")))
          ))))

#_(deftest firehose-can-load
  (with-faked-responses
    (let [user (s/create-test-user)
          swirl (s/create-swirl "website" (user :id) "The latest swirl" "This is a great swirl" [])]
      (-> (session app)
          (visit "/swirls")
          (print-session)
          (press :.next-swirls)
          (press :.previous-swirls)
          (follow (swirl :title))
          (within [:h1]
                  (has (some-text? (swirl :title))))

          ))))

#_(deftest home-page-can-paginate
  (let [user (s/create-test-user)]
    (-> (session app)
        (visit "/")
        (follow "Next 20 swirls >")
        (follow "< Previous 20 swirls")
        (login-as user)
        (follow "Next 20 swirls >")
        (follow "< Previous 20 swirls")
        )))

(deftest can-create-a-private-swirl
  (with-faked-responses
    (let [author (s/create-test-user)
          added (s/create-test-user)
          not-added (s/create-test-user)
          test-state (atom {})]
      (-> (session app)
          (visit "/")
          ; Login as the author
          (actions/follow-login-link)
          (login-as author)

          ; Create a swirl
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")

          (fill-in :.recipients (:username added))

          (assert-is-private-checkbox-is-not-checked)

          ; Make the Swirl Private
          (check :.private-toggle)

          (assert-is-private-checkbox-is-checked)


          (fill-in :#swirl-title (str (:id author)))        ; odd title, but makes it unique :)

          (actions/save-swirl)
          (save-url test-state :view-swirl-uri)

          ; go to firehose and check can see the swirl listed

          (visit "/swirls")

          (follow (str (:id author)))
          (assert-swirl-title-in-header "watch" (str (:id author)))


          ; Now login as the added user

          (actions/log-out)
          (actions/follow-login-link)
          (login-as added)

          ;added user can see the swirl
          (visit (@test-state :view-swirl-uri))

          (assert-swirl-title-in-header "watch" (str (:id author)))

          ; go to firehose and check can see the swirl listed

          (visit "/swirls")

          (follow (str (:id author)))
          (assert-swirl-title-in-header "watch" (str (:id author)))

          ; Now login as the not-added user

          (actions/log-out)
          (actions/follow-login-link)
          (login-as not-added)

          ;not-added user can't see the swirl
          (visit (@test-state :view-swirl-uri))
          (has (status? 404))
          (has (text? "Not Found"))

          ; go to firehose and check cannot see the swirl listed

          (visit "/swirls")

          (cannot-follow (str (:id author)))

          ;nor can a logged out user
          (visit "/")

          (actions/log-out)

          (visit (@test-state :view-swirl-uri))
          (has (status? 404))
          (has (text? "Not Found"))

          ; go to firehose and check cannot see the swirl listed

          (visit "/swirls")
          (cannot-follow (str (:id author)))

          ;now log-in back as author and turn off the privacy
          (visit "/")
          (actions/follow-login-link)
          (actions/login-as author)

          (visit (@test-state :view-swirl-uri))
          (follow "Edit Swirl")

          (assert-is-private-checkbox-is-checked)

          (uncheck :.private-toggle)

          (actions/save-swirl)

          (actions/log-out)

          ;now anyone can see it

          (visit (@test-state :view-swirl-uri))

          (assert-swirl-title-in-header "watch" (str (:id author)))

          ; go to firehose and check can see the swirl listed

          (visit "/swirls")

          (follow (str (:id author)))
          (assert-swirl-title-in-header "watch" (str (:id author)))

          )
      )))

(deftest can-change-the-type-of-swirl
  (with-faked-responses
    (let [author (s/create-test-user)
          test-state (atom {})]
      (-> (session app)
          (visit "/")
          ; Login as the author
          (actions/follow-login-link)
          (login-as author)

          ; Create a website swirl
          (actions/follow-create-link)
          (fill-in "Enter a website link" "http://exact.match.com/youtube.onions.html")
          (actions/submit "Go")

          (assert-default-selection-is-value "video")

          (actions/save-swirl)

          (save-swirl-id test-state :swirl-id)

          ;type should be website

          (assert-swirl-type (@test-state :swirl-id) "video")

          ; Now back to edit
          (follow "Edit Swirl")

          (assert-default-selection-is-value "video")

          ; let's choose movie instead

          (choose :.type-selector "movie")

          (actions/save-swirl)

          ; type should now be video

          (assert-swirl-type (@test-state :swirl-id) "movie")

          ))))
