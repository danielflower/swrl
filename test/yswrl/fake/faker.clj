(ns yswrl.fake.faker)

(defmacro with-faked-responses [body]
  (list 'with-fake-routes {

                           ; Get YouTube video info
                           #"https:\/\/www\.googleapis\.com\/youtube\/v3\/videos\?part=snippet%2Cplayer&id=(.+)&key=AIzaSyCuxJgvMSqJbJxVYAUOINsoTjs2DuFsLMg"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/youtube." ((ring.util.codec/form-decode (req :query-string)) "id") ".json"))})


                           ; Run an iTunes albums search
                           #"https:\/\/itunes\.apple\.com\/search\?term=(.+)&media=music&entity=album"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/itunes.album." ((ring.util.codec/form-decode (req :query-string)) "term") ".json"))})
                           ;

                           ; Run an amazon albums search
                           "http://webservices.amazon.com/onca/xml"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/amazon.book." ((ring.util.codec/form-decode (req :query-string)) "Keywords") ".xml"))})

                           }
        body
        ))

