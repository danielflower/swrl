(ns yswrl.swirls.types)

(def types {

            "book" {
                    :words {
                            :watch "read"
                            :seen "read"
                            :seen-it "seen it"
                            }
                    }

            "album" {
                     :words {
                             :watch "listen to"
                             :seen "heard"
                             :seen-it "heard it"
                             }
                     }

            "video" {
                       :words {
                               :watch "watch"
                               :seen "seen"
                               :seen-it "watched it"
                               }
                       }

            "movie" {
                     :words {
                             :watch "watch"
                             :seen "seen"
                             :seen-it "seen it"
                             }
                     }

            "website" {
                       :words {
                               :watch "see"
                               :seen "seen"
                               :seen-it "visited it"
                               }
                       }

            "generic" {
                       :words {
                               :watch "see"
                               :seen "seen"
                               :seen-it "seen it"
                               }
                       }

            })

(defn type-of [swirl]
  (if-let [type (get types (swirl :type))]
    type
    (throw (Exception. (str "No type found for" swirl)))
    ))