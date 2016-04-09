(ns yswrl.swirls.types)



(def types {

            "book"    {
                       :name  "book"
                       :words {
                               :watch    "read"
                               :watching "Reading"
                               :seen     "read"
                               :seen-it  "seen it"
                               }
                       }

            "game"    {
                       :name  "game"
                       :words {
                               :watch    "play"
                               :watching "Playing"
                               :seen     "played"
                               :seen-it  "played it"
                               }
                       }

            "album"   {
                       :name  "album"
                       :words {
                               :watch    "listen to"
                               :watching "Listening"
                               :seen     "heard"
                               :seen-it  "heard it"
                               }
                       }

            "video"   {
                       :name  "video"
                       :words {
                               :watch    "watch"
                               :watching "Watching"
                               :seen     "seen"
                               :seen-it  "watched it"
                               }
                       }

            "movie"   {
                       :name  "movie"
                       :words {
                               :watch    "watch"
                               :watching "Watching"
                               :seen     "seen"
                               :seen-it  "seen it"
                               }
                       }

            "tv"      {
                       :name  "tv"
                       :words {
                               :watch    "watch"
                               :watching "Watching"
                               :seen     "seen"
                               :seen-it  "seen it"
                               }
                       }

            "website" {
                       :name  "website"
                       :words {
                               :watch    "see"
                               :watching "Looking"
                               :seen     "seen"
                               :seen-it  "visited it"
                               }
                       }
            "podcast" {
                       :name  "podcast"
                       :words {
                               :watch    "listen to"
                               :watching "Listening"
                               :seen     "heard"
                               :seen-it  "heard it"
                               }
                       }
            "app"     {
                       :name  "app"
                       :words {
                               :watch    "get"
                               :watching "Using"
                               :seen     "got"
                               :seen-it  "got it"
                               }
                       }

            "unknown"     {
                       :name  "unknown"
                       :words {
                               :watch    "check out"
                               :watching "checking out"
                               :seen     "checked out"
                               :seen-it  "checked out"
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