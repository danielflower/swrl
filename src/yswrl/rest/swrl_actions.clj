(ns yswrl.rest.swrl-actions
  (:require
    [compojure.core :refer [defroutes context ]]
    [yswrl.swirls.swirl-routes :as swirl-routes])
  )


(defroutes swirl-actions-routes
           (context "/api/v1/swrl-actions" []
             (swirl-routes/post-response-api-route)
             (swirl-routes/create-swrl-app-api)
                    ))