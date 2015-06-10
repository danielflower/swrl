(ns yswrl.rest.swirl-resource
  (:require
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.response :refer [status redirect response not-found]]
    [compojure.core :refer [defroutes context GET POST]]
    [yswrl.swirls.swirl-routes :as swirl-routes]))


(defn do-it []
  (println "Do it")
  (response {:hello "Little"}))

(defroutes swirl-rest-routes
           (context "/api/v1/swirls" []
             (defroutes swirl-rest-routes-erm
                        (GET "/" [] (do-it))
                        (swirl-routes/post-response-route "")
                        (swirl-routes/post-comment-route "")
                        ))
           )
