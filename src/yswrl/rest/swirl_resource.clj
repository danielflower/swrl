(ns yswrl.rest.swirl-resource
  (:require
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.response :refer [status redirect response not-found]]
    [compojure.core :refer [defroutes context GET POST]]
    [yswrl.swirls.swirl-routes :as swirl-routes]
    [yswrl.auth.guard :as guard]))


(defn do-it []
  (println "Do it")
  (response {:hello "Little"}))

(defroutes swirl-rest-routes
           (context "/api/v1/swirls" []
             (defroutes swirl-rest-routes-erm
                        (GET "/" [] (do-it))
                        (swirl-routes/post-response-route "")
                        ))
           )

; Example Javascript:
;fetch('/api/v1/swirls/5560/respond', {
;                                      method: 'post',
;                                      credentials: 'same-origin',
;                                      headers: {
;                                                'Accept': 'application/json',
;                                                'Content-Type': 'application/json'
;                                                },
;                                      body: JSON.stringify({
;                                                            id: 5560,
;                                                            responseButton: 'Loved it', "response-summary": ""
;})
;}).then(function (resp) { console.log('Got response', resp); });
;
