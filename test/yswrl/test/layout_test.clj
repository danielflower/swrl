(ns yswrl.test.layout-test
  (:use midje.sweet)
  (:require [yswrl.layout :as layout]
            [yswrl.links :as links]))

(facts "about generating html"
       (fact "generates the correct gravatar img html"
             (layout/generate-gravatar-img-html ..email-hash.. ..size..)
             => "<img class=\"gravatar\" src=\"..gravatar-url..\" width=\"..size..\" height=\"..size..\" alt=\"\">"
             (provided
               (links/gravatar-url ..email-hash.. ..size..)
               => ..gravatar-url..))

       (fact "generates the correct user-selector label"
             (layout/generate-user-selector-label ..email-hash.. ..username..)
             => "<input id=\"..username..\" type=\"checkbox\" name=\"who\" value=\"..username..\" checked><label for=\"..username..\">..gravatar-img-html....username..</label>"
             (provided
               (layout/generate-gravatar-img-html ..email-hash.. 35)
               => ..gravatar-img-html..)))
