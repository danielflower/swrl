(ns yswrl.test.swirls.boardgamegeek-test
  (:require [yswrl.swirls.boardgamegeek :as bgg])
  (:use midje.sweet))

(facts "about parsing search results from boardgamegeek api"
       (fact "can parse a response with results"
             (bgg/search "mottainai")
             => {:results [{:title           "Mottainai"
                            :url             "https://boardgamegeek.com/boardgame/175199"
                            :bgg-id          "175199"
                            :thumbnail-url   "https://cf.geekdo-images.com/images/pic2688214.jpg"
                            :large-image-url "https://cf.geekdo-images.com/images/pic2688214.jpg"
                            :overview        "&quot;Mottainai&quot; (pronounced mot/tai/nai or like the English words mote-tie-nigh) means &quot;Don't waste&quot;, or &quot;Every little thing has a soul&quot;. In the game Mottainai, a successor in the Glory to Rome line, you use your cards for many purposes. Each player is a monk in a temple who performs tasks, collects materials, and sells or completes works for visitors. Every card can be each of these three things.<br/><br/>You choose tasks to allow you to perform actions, keeping in mind that other players will get to follow up on your task on their next turn. Clever planning and combining of your works' special abilities is key, as is managing which materials you sell.<br/><br/>Mottainai is a quick, but deep, game experience.<br/><br/>"
                            :categories      ["Card Game"]
                            :designer        "Carl Chudyk, Another Designer"
                            :min-players     "2"
                            :max-players     "5"
                            :min-playtime    "15"
                            :max-playtime    "30"
                            }
                           {:title           "Mottainai: Wutai Mountain"
                            :url             "https://boardgamegeek.com/boardgame/220186"
                            :bgg-id          "220186"
                            :thumbnail-url   "https://cf.geekdo-images.com/images/pic3406882.jpg"
                            :large-image-url "https://cf.geekdo-images.com/images/pic3406882.jpg"
                            :overview        "Mottainai: Wutai Mountain, an expansion for Mottainai that you need only one copy of regardless of player count, is a deck of &quot;Om&quot; works. Each such work can gather its own set of helpers, materials, and sales tucked underneath it, and it has a special ability that grants benefits based on those cards. Expect even more exciting and powerful combinations!<br/><br/>"
                            :categories      ["Card Game" "Expansion for Base-game"]
                            :designer        "Carl Chudyk"
                            :min-players     "2"
                            :max-players     "5"
                            :min-playtime    "0"
                            :max-playtime    "0"
                            }]}
             (provided
               (bgg/get-raw-results "mottainai")
               => (slurp "test/yswrl/fake/bgg-mottainai.xml")
               (bgg/get-raw-details "175199")
               => (slurp "test/yswrl/fake/bgg-175199.xml")
               (bgg/get-raw-details "220186")
               => (slurp "test/yswrl/fake/bgg-220186.xml"))))