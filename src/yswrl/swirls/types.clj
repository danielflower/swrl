(ns yswrl.swirls.types)

(def types {

            "book" {
                    :words {
                            :watch "read"
                            :seen "read"
                            }
                    :default-seen-responses [ "Loved it" "Hated it" "Meh" ]
                    :default-not-seen-responses [ "Not interested" "Read it later" ]
                    }

            "album" {
                     :words {
                             :watch "listen to"
                             :seen "heard"
                             }
                     :default-seen-responses [ "Loved it" "Hated it" "Meh" ]
                     :default-not-seen-responses [ "Not interested" "Later" ]
                     }

            "youtube" {
                       :words {
                               :watch "watch"
                               :seen "seen"
                               }
                       :default-seen-responses [ "Loved it" "Hated it" "Meh" ]
                       :default-not-seen-responses [ "Not interested" "Watch it later" ]
                       }

            "movie" {
                     :words {
                             :watch "watch"
                             :seen "seen"
                             }
                     :default-seen-responses ["Loved it" "Hated it" "Meh"]
                     :default-not-seen-responses [ "Not interested" "Watch it later" ]
                     }

            "website" {
                       :words {
                               :watch "see"
                               :seen "seen"
                               }
                       :default-seen-responses [ "Interesting" "Boring" "Meh" ]
                       :default-not-seen-responses [ "Not interested" "Later" ]
                       }

            "generic" {
                       :words {
                               :watch "see"
                               :seen "seen"
                               }
                       :default-seen-responses [ "Wow" "What the..." "Meh" ]
                       :default-not-seen-responses [ "Not interested" "Later" ]
                       }

            })

(defn type-of [swirl]
  (if-let [type (get types (swirl :type))]
    type
    (throw (Exception. (str "No type found for" swirl)))
    ))