(ns yswrl.features.auth-features
  (:require [yswrl.handler :refer [app]]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [clojure.test :refer :all]))
(selmer.parser/cache-off!)

(defn now [] (System/currentTimeMillis))

(deftest homepage-greeting
  (-> (session app)
      (visit "/")
      (within [:h1]
                (has (text? "Welcome to Swirl")))))

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
        (follow username)
        (within [:h1]
                (has (text? (str "Reviews by " username))))

        ; Logout
        (follow "Log out")
        (follow-redirect)
        (within [:h1]
                (has (text? "You are now logged out")))

        ; Login with wrong username
        (follow "Login")
        (fill-in "Username" username)
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

(deftest invalid-token-for-password-reset
    (-> (session app)
        (visit "/reset-password?token=someinvalidtoken")
        (within [:h1]
                (has (text? "Reset password")))
        (fill-in "New password" "whatever")
        (press "Change password")
        (within [:.validation-error]
                (has (text? "Sorry, that request was invalid. Please go to the login page and request a new password reset.")))))