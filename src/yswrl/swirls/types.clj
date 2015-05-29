(ns yswrl.swirls.types)



(def types {

            "book"    {
                       :name  "book"
                       :words {
                               :watch   "read"
                               :seen    "read"
                               :seen-it "seen it"
                               }
                       }

            "album"   {
                       :name  "album"
                       :words {
                               :watch   "listen to"
                               :seen    "heard"
                               :seen-it "heard it"
                               }
                       }

            "video"   {
                       :name  "video"
                       :words {
                               :watch   "watch"
                               :seen    "seen"
                               :seen-it "watched it"
                               }
                       }

            "movie"   {
                       :name  "movie"
                       :words {
                               :watch   "watch"
                               :seen    "seen"
                               :seen-it "seen it"
                               }
                       }

            "website" {
                       :name  "website"
                       :words {
                               :watch   "see"
                               :seen    "seen"
                               :seen-it "visited it"
                               }
                       }

            "generic" {
                       :name  "generic"
                       :words {
                               :watch   "see"
                               :seen    "seen"
                               :seen-it "seen it"
                               }
                       }

            })

(defn type-of [swirl]
  (if-let [type (get types (swirl :type))]
    type
    (throw (Exception. (str "No type found for" swirl)))
    ))

(defn from-open-graph-type [og-type]
  (if-let [type (get types og-type)]
    (type :name)
    "website"))