(ns yswrl.fake.faker
  (:require [ring.util.codec :refer [form-decode]]))

(defmacro with-faked-responses [body]
  (list 'with-fake-routes {

                           ; Get YouTube video info
                           #"https:\/\/www\.googleapis\.com\/youtube\/v3\/videos\?part=snippet%2Cplayer&id=(.+)&key=AIzaSyCuxJgvMSqJbJxVYAUOINsoTjs2DuFsLMg"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/youtube." ((form-decode (req :query-string)) "id") ".json"))})


                           ; Run an iTunes albums search
                           #"https:\/\/itunes\.apple\.com\/search\?term=(.+)&media=music&entity=album"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/itunes.album-search." ((form-decode (req :query-string)) "term") ".json"))})
                           ;

                           ; Run an iTunes album lookup
                           #"https:\/\/itunes\.apple\.com\/lookup\?id=(.+)&entity=song"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/itunes.album." ((form-decode (req :query-string)) "id") ".json"))})
                           ;

                           ; Run an amazon albums search
                           "http://webservices.amazon.com/onca/xml"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/amazon.book." ((form-decode (req :query-string)) "Keywords") ".xml"))})
                           ;

                           ; get the facebook access token
                           #"https:\/\/graph\.facebook\.com\/oauth\/access_token.*"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/facebook.oauth.access_token."((form-decode (req :query-string)) "code") ".txt"))})
                           ;

                           ; get the facebook user details
                           #"https:\/\/graph\.facebook\.com\/me\?access_token=.*"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/facebook.user-details." ((form-decode (req :query-string)) "access_token") ".json"))})
                           }
        body
        ))

