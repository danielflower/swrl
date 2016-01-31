(ns yswrl.rest.swirl-resource
  (:require
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.response :refer [status redirect response not-found]]
    [compojure.core :refer [defroutes context GET POST]]
    [yswrl.swirls.swirl-routes :as swirl-routes]))

(defroutes swirl-rest-routes
           (context "/api/v1/swirls" []
             (defroutes swirl-rest-routes-erm

                        (swirl-routes/get-swirls-by-id)
                        (swirl-routes/search-for-swirls)

                        (GET "/:id{[0-9]+}/comments" [id comment-id-start :as req]
                          (swirl-routes/get-html-of-comments-since
                            (swirl-routes/session-from req)
                            (Long/parseLong id)
                            (if (clojure.string/blank? comment-id-start) 0 (Long/parseLong comment-id-start))))
                        (swirl-routes/post-response-route "")
                        (swirl-routes/post-comment-route "")
                        ))
           )
