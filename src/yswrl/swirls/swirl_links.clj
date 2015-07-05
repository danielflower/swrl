(ns yswrl.swirls.swirl-links)

(def itunes-id {
                :code "I"
                :name "iTunes ID"
                })
(def amazon-asin {
                  :code "A"
                  :name "Amazon ASIN ID"
                  })
(def amazon-url {
                  :code "Z"
                  :name "Amazon Affiliate URL"
                  })

(def website-url {
                  :code "W"
                  :name "Website URL"
                  })
(def imdb-id {
              :code "M"
              :name "IMDB ID"
              })

(def swirl-response {
                     :code "S"
                     :name "Swirl Response"
                     })

(def swirl-progenitor {
                     :code "P"
                     :name "Swirl Progenitor"
                     })


(def link-types {

                 (itunes-id :code) itunes-id,
                 (amazon-asin :code) amazon-asin,
                 (amazon-url :code) amazon-url,
                 (website-url :code) website-url,
                 (imdb-id :code) imdb-id
                 (swirl-response :code) swirl-response
                 (swirl-progenitor :code) swirl-progenitor

                 })

(defn link-type-of [swirl-link]
  (if-let [type (get link-types swirl-link)]
    type
    (throw (Exception. (str "No link type found for" swirl-link)))
    ))
