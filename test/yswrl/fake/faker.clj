(ns yswrl.fake.faker
  (:require [ring.util.codec :refer [form-decode]]))

(defmacro with-faked-responses [body]
  (list 'with-fake-routes {

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
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/facebook.oauth.access_token." ((form-decode (req :query-string)) "code") ".txt"))})
                           ;

                           ; get the facebook user details
                           #"https:\/\/graph\.facebook\.com\/me\?access_token=.*"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/facebook.user-details." ((form-decode (req :query-string)) "access_token") ".json"))})
                           ;

                           ; Run a tmdb movie search
                           #"https:\/\/api\.themoviedb\.org\/3\/search\/movie\?api_key=(.+)&query=(.+)"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/tmdb.movie-search." ((form-decode (req :query-string)) "query") ".json"))})
                           ;

                           ; Run a tmdb tv search
                           #"https:\/\/api\.themoviedb\.org\/3\/search\/tv\?api_key=(.+)&query=(.+)"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/tmdb.tv-search." ((form-decode (req :query-string)) "query") ".json"))})
                           ;

                           ;get a movie from tmdb id
                           #"https:\/\/api\.themoviedb\.org\/3\/movie\/(.+)\?api_key=(.+)"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/tmdb.get-movie-from-tmdb-id." (get (re-find #"\/3\/movie\/(.+)$" (req :uri)) 1) ".json"))})
                           ;

                           ;get a tv show from tmdb id
                           #"https:\/\/api\.themoviedb\.org\/3\/tv\/(.+)\?api_key=(.+)"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/tmdb.get-tv-from-tmdb-id." (get (re-find #"\/3\/tv\/(.+)$" (req :uri)) 1) ".json"))})
                           ;

                           ;get a movie from imdb id
                           #"https:\/\/api\.themoviedb\.org\/3\/find\/(.+)\?api_key=(.+)&external_source=imdb_id"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/tmdb.tmdb-find-by-imdb-id." (get (re-find #"\/3\/find\/(.+)$" (req :uri)) 1) ".json"))})
                           ;

                           ;get a movie from omdb http://www.omdbapi.com/?apikey=d33a4ae1&i=tt0333766
                           #"http:\/\/www\.omdbapi\.com\/\?apikey=d33a4ae1&i=tt0333766"
                           (fn [req] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/omdb-tt0333766.json"))})
                           ;

                           ;get metadata from an imdb url
                           #"http:\/\/www\.imdb\.com\/title\/tt1486217\/\?ref_=nv_sr_1"
                           (fn [_] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/website.get-metadata.imdb.html"))})
                           ;

                           ;get metadata from an imdb url
                           #"http:\/\/jakearchibald\.com\/2013\/progressive\-enhancement\-still\-important/"
                           (fn [_] {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/website.get-metadata.jakearchibald.html"))})
                           ;

                           ; Just load the page with the filename taken from the URL's path
                           #"http:\/\/exact\.match\.com\/.+"
                           (fn [req]
                             (let [page (get (re-find #"\/(.+)$" (req :uri)) 1)]
                               {:status 200 :headers {} :body (slurp (str "test/yswrl/fake/exact/" page))}))
                           ;

                           ; junk page fails
                           #"http:\/\/www\.fjljldjfjdsoifjsdf.com\/"
                           (fn [req]
                             (throw (Exception. "website down")))
                           }
        body
        ))

