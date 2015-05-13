(ns yswrl.fake.faker)

(defmacro with-faked-responses [body]
  (list 'with-fake-routes { #"https:\/\/www\.googleapis\.com\/youtube\/v3\/videos\?part=snippet%2Cplayer&id=(.+)&key=AIzaSyCuxJgvMSqJbJxVYAUOINsoTjs2DuFsLMg"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/youtube." ((ring.util.codec/form-decode (req :query-string)) "id") ".json")) })}
        body
        ))

