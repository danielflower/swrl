(ns yswrl.features.auth-features
  (:require [yswrl.handler :refer [app]]
            [yswrl.features.actions :as actions]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]
            [yswrl.test.scaffolding :as s]
            [yswrl.links :as links]))
(selmer.parser/cache-off!)

(defn now [] (System/currentTimeMillis))

(deftest registration
  (let [username (str "test-user" (now))
        email (str username "@example.org")
        password "Abcd1234"]

    (-> (session app)
        (visit "/register")
        (within [:h1]
                (has (text? "Register")))
        (fill-in "Username" username)
        (fill-in "Email" email)
        (fill-in "Password" password)
        (fill-in "Confirm Password" password)
        (press "Register")
        (follow-redirect)
        (follow [:.logged-in-user])
        (within [:h1]
                (has (text? (str "Edit your profile"))))
        (within [:main :h2]
                (has (text? (str "Swirls you've made"))))

        ; Logout
        (actions/log-out)
        (within [:h1]
                (has (text? "You are now logged out")))

        ; Login with wrong username
        (actions/follow-login-link)
        (fill-in "Username or email" username)
        (fill-in "Password" "wrong password")
        (press "Login")
        (within [:.validation-error]
                (has (text? "Sorry, you have entered an invalid username or password. Please try again.")))

        ; Use forgotten password link
        (follow "Forgot your password? Click here.")
        (fill-in "Username or email" (str "invalid" (now)))
        (press "Continue")
        (within [:.validation-error]
                (has (text? "No user with that email or username was found. Click here to register.")))
        (fill-in "Username or email" username)
        (press "Continue")
        (follow-redirect)
        (within [:h1]
                (has (text? "Please check your email")))

        ; can't access email so can't test forgotten-password unfortunately. So just log in

        )))

(deftest update-profile
  (let [user (s/create-test-user)
        another-user (s/create-test-user)]

    (-> (session app)
        (visit (links/inbox))
        (follow-redirect)
        (actions/login-as user)

        (visit (links/user (user :username)))
        (within [:h1]
                (has (text? "Edit your profile")))
        (follow "Update your username or email")
        (within [:h1]
                (has (text? "Edit your profile")))
        (fill-in "Username" (another-user :username))
        (fill-in "Email" (user :email))
        (press "Update your settings")
        (within [:.validation-error]
                (has (text? "Sorry, there was a little problem with your details.(\"A user with that username already exists. Please select a different username.\")")))

        (fill-in "Username" (user :username))
        (fill-in "Email" (another-user :email))
        (press "Update your settings")
        (within [:.validation-error]
                (has (text? "Sorry, there was a little problem with your details.(\"A user with that email already exists. Please select a different email.\")")))

        (fill-in "Username" (str (user :username) "-updated"))
        (fill-in "Email" (str (user :email) ".updated"))
        (press "Update your settings")
        (follow-redirect)

        (actions/log-out)
        (actions/follow-login-link)
        (actions/login-as {:username (str (user :username) "-updated")})

        )))

(deftest invalid-token-for-password-reset
    (-> (session app)
        (visit "/reset-password?token=someinvalidtoken")
        (within [:h1]
                (has (text? "Reset password")))
        (fill-in "New password" "whatever")
        (press "Change password")
        (within [:.validation-error]
                (has (text? "Sorry, that request was invalid. Please go to the login page and request a new password reset.")))))