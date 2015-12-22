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

            "game"    {
                       :name  "game"
                       :words {
                               :watch   "play"
                               :seen    "played"
                               :seen-it "played it"
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

            "tv"   {
                       :name  "tv"
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
            "podcast" {
                       :name  "podcast"
                       :words {
                               :watch   "listen to"
                               :seen    "heard"
                               :seen-it "heard it"
                               }
                       }
            "app" {
                       :name  "app"
                       :words {
                               :watch   "get"
                               :seen    "got"
                               :seen-it "got it"
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