(ns yswrl.test.layout-test
  (:use midje.sweet)
  (:require [yswrl.layout :as layout]
            [yswrl.links :as links]))

(facts "about generating html"
       (fact "generates the correct gravatar img html"
             (layout/generate-avatar-img-html ..link.. ..size..)
             => "<img class=\"gravatar\" src=\"..link..\" width=\"..size..\" height=\"..size..\" alt=\"\">")

       (fact "generates the correct user-selector label"
             (layout/generate-user-selector-label ..username..)
             => "<input id=\"..username..\" type=\"checkbox\" name=\"who\" value=\"..username..\" checked><label for=\"..username..\">..gravatar-img-html....username..</label>"
             (provided
               (layout/generate-avatar-img-html (layout/get-avatar-link ..username.. 35) 35)
               => ..gravatar-img-html..)))
